package com.trials.deck_tiering.service;

import com.trials.deck_tiering.model.Deck;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Glicko-2 rating updates for 1v1 deck matches.
 */
@Component
public class Glicko2Calculation {

    // --- Tier thresholds ---
    private final int TIER_1_RATING = 2000;
    private final int TIER_2_RATING = 1750;
    private final int TIER_3_RATING = 1400;
    private final int TIER_4_RATING = 1000;
    private final int TIER_5_RATING = 750;

    // --- Glicko-2 constants ---
    private static final double TAU = 0.5;
    private static final double EPSILON = 1e-6;

    private static final double SCALE = 173.7178;

    // --- Numeric safety ---
    private static final double E_EPS = 1e-12;      // clamp E away from 0/1
    private static final double MIN_VINV = 1e-18;   // stop vInv collapsing to 0
    private static final int MAX_BRACKET_ITERS = 10_000;
    private static final int MAX_ROOT_ITERS = 1_000;

    public List<Deck> update1v1Match(Deck deckA, Deck deckB, int winsA, int winsB) {
        if (deckA == null || deckB == null) {
            throw new IllegalArgumentException("deckA and deckB must not be null");
        }
        if (winsA < 0 || winsB < 0) {
            throw new IllegalArgumentException("winsA/winsB must be >= 0");
        }
        if (winsA == winsB) {
            throw new IllegalArgumentException("Match must have a winner. winsA cannot equal winsB.");
        }

        List<Double> scoresForA = new ArrayList<>(winsA + winsB);
        for (int i = 0; i < winsA; i++) scoresForA.add(1.0);
        for (int i = 0; i < winsB; i++) scoresForA.add(0.0);

        applySeriesAsRatingPeriod(deckA, deckB, scoresForA);

        deckA.setTier(deckTierCalculation(deckA.getRating()));
        deckB.setTier(deckTierCalculation(deckB.getRating()));

        List<Deck> updated = new ArrayList<>(2);
        updated.add(deckA);
        updated.add(deckB);
        return updated;
    }

    private void applySeriesAsRatingPeriod(Deck deckA, Deck deckB, List<Double> scoresForA) {
        double ratingA = deckA.getRating();
        double devA = deckA.getRatingDeviation();
        double volA = deckA.getVolatility();

        double ratingB = deckB.getRating();
        double devB = deckB.getRatingDeviation();
        double volB = deckB.getVolatility();

        if (devA <= 0 || devB <= 0) {
            throw new IllegalStateException("ratingDeviation must be > 0 for both decks (check persistence / migration).");
        }
        if (volA <= 0 || volB <= 0) {
            throw new IllegalStateException("volatility must be > 0 for both decks (check persistence / migration).");
        }

        double muA = (ratingA - 1500.0) / SCALE;
        double phiA = devA / SCALE;

        double muB = (ratingB - 1500.0) / SCALE;
        double phiB = devB / SCALE;

        int n = scoresForA.size();
        double[] muOppForA = new double[n];
        double[] phiOppForA = new double[n];
        double[] sForA = new double[n];

        double[] muOppForB = new double[n];
        double[] phiOppForB = new double[n];
        double[] sForB = new double[n];

        for (int i = 0; i < n; i++) {
            muOppForA[i] = muB;
            phiOppForA[i] = phiB;
            sForA[i] = scoresForA.get(i);

            muOppForB[i] = muA;
            phiOppForB[i] = phiA;
            sForB[i] = 1.0 - sForA[i];
        }

        double[] updatedA = glicko2UpdateRatingPeriod(muA, phiA, volA, muOppForA, phiOppForA, sForA);
        double[] updatedB = glicko2UpdateRatingPeriod(muB, phiB, volB, muOppForB, phiOppForB, sForB);

        double newRatingA = 1500.0 + SCALE * updatedA[0];
        double newDevA = SCALE * updatedA[1];
        double newVolA = updatedA[2];

        double newRatingB = 1500.0 + SCALE * updatedB[0];
        double newDevB = SCALE * updatedB[1];
        double newVolB = updatedB[2];

        newDevA = clamp(newDevA, 30.0, 350.0);
        newDevB = clamp(newDevB, 30.0, 350.0);

        deckA.setRating((int) Math.round(newRatingA));
        deckA.setRatingDeviation(newDevA);
        deckA.setVolatility(newVolA);

        deckB.setRating((int) Math.round(newRatingB));
        deckB.setRatingDeviation(newDevB);
        deckB.setVolatility(newVolB);
    }

    private int deckTierCalculation(int rating) {
        if (rating >= TIER_1_RATING) return 1;
        if (rating >= TIER_2_RATING) return 2;
        if (rating >= TIER_3_RATING) return 3;
        if (rating >= TIER_4_RATING) return 4;
        if (rating >= TIER_5_RATING) return 5;
        return 6;
    }

    private double[] glicko2UpdateRatingPeriod(
            double mu,
            double phi,
            double sigma,
            double[] muOpp,
            double[] phiOpp,
            double[] s
    ) {
        double vInv = 0.0;

        for (int i = 0; i < s.length; i++) {
            double g = g(phiOpp[i]);
            double E = E(mu, muOpp[i], phiOpp[i]);
            vInv += (g * g) * E * (1.0 - E);
        }

        // ---- FIX: prevent vInv -> 0 causing v -> infinity ----
        if (vInv < MIN_VINV) vInv = MIN_VINV;

        double v = 1.0 / vInv;

        double deltaSum = 0.0;
        for (int i = 0; i < s.length; i++) {
            double g = g(phiOpp[i]);
            double E = E(mu, muOpp[i], phiOpp[i]);
            deltaSum += g * (s[i] - E);
        }

        double delta = v * deltaSum;

        double a = Math.log(sigma * sigma);
        double A = a;
        double B;

        if (delta * delta > phi * phi + v) {
            B = Math.log(delta * delta - phi * phi - v);
        } else {
            double k = 1.0;
            B = A - k * TAU;

            // ---- FIX: hard cap bracketing loop ----
            int guard = 0;
            while (f(B, delta, phi, v, a) > 0.0) {
                k += 1.0;
                B = A - k * TAU;

                if (++guard > MAX_BRACKET_ITERS) {
                    // fallback: force a wide bracket and continue
                    B = A - 50.0 * TAU;
                    break;
                }
            }
        }

        double fA = f(A, delta, phi, v, a);
        double fB = f(B, delta, phi, v, a);

        // ---- FIX: hard cap root finding loop ----
        int iters = 0;
        while (Math.abs(B - A) > EPSILON) {
            double denom = (fB - fA);

            // avoid division by ~0
            if (Math.abs(denom) < 1e-18) {
                break;
            }

            double C = A + (A - B) * fA / denom;
            double fC = f(C, delta, phi, v, a);

            if (fC * fB < 0.0) {
                A = B;
                fA = fB;
            } else {
                fA = fA / 2.0;
            }
            B = C;
            fB = fC;

            if (++iters > MAX_ROOT_ITERS) {
                break;
            }
        }

        double sigmaPrime = Math.exp(A / 2.0);

        double phiStar = Math.sqrt(phi * phi + sigmaPrime * sigmaPrime);

        double phiPrime = 1.0 / Math.sqrt((1.0 / (phiStar * phiStar)) + (1.0 / v));

        double muPrimeSum = 0.0;
        for (int i = 0; i < s.length; i++) {
            double g = g(phiOpp[i]);
            double E = E(mu, muOpp[i], phiOpp[i]);
            muPrimeSum += g * (s[i] - E);
        }

        double muPrime = mu + (phiPrime * phiPrime) * muPrimeSum;

        return new double[]{muPrime, phiPrime, sigmaPrime};
    }

    private double g(double phi) {
        return 1.0 / Math.sqrt(1.0 + (3.0 * phi * phi) / (Math.PI * Math.PI));
    }

    // ---- FIX: clamp E away from 0/1 to prevent vInv collapse ----
    private double E(double mu, double muJ, double phiJ) {
        double e = 1.0 / (1.0 + Math.exp(-g(phiJ) * (mu - muJ)));
        if (e < E_EPS) return E_EPS;
        if (e > 1.0 - E_EPS) return 1.0 - E_EPS;
        return e;
    }

    private double f(double x, double delta, double phi, double v, double a) {
        double ex = Math.exp(x);
        double num = ex * (delta * delta - phi * phi - v - ex);
        double den = 2.0 * Math.pow(phi * phi + v + ex, 2.0);
        return (num / den) - ((x - a) / (TAU * TAU));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
