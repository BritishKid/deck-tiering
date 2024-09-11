package com.trials.deck_tiering.controller;

import com.trials.deck_tiering.model.Deck;
import com.trials.deck_tiering.service.DeckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Controller
public class DeckListController {

    @Autowired
    private DeckService deckService;

    @RequestMapping("/")
    public String index() {
        return "index.html";
    }

    @GetMapping("/decks/game/{game}")
    public String getDeckList(@PathVariable("game") String game,
                              Model model) {
        List<Deck> allUniqueDecks = deckService.getAllUniqueDecks();
        model.addAttribute("decklist", deckService.filterByGame(allUniqueDecks, game));
        model.addAttribute("title", game + " decks");
        model.addAttribute("game", game);
        return "decklist";
    }

    @PostMapping(path="/deck/winners={winners}&losers={losers}")
    public String updateResults(@PathVariable("winners") String winnerIds,
                                @PathVariable("losers") String losersIds,
                                Model model) {

        String[] winners = winnerIds.split(","); //this allows for multi games in future for now handle 1v1
        String[] losers = losersIds.split(",");

        deckService.updateDeckRatings(winners, losers);

        model.addAttribute("decklist", deckService.getAllUniqueDecks());
        model.addAttribute("game", "NEED TO ADD GAME");
        return "decklist";
    }

    @RequestMapping(path="/deck/addoutcome")
    public String createOutcome(Model model) {
        List<Deck> allUniqueDecks = deckService.getAllUniqueDecks();
        model.addAttribute("decklist", deckService.orderDeckByRating(allUniqueDecks));
        return "addoutcome";
    }

    @PostMapping(path="/deck/resultsupdate")
    public String updateResult(@ModelAttribute("winningDeck") String winningDeck,
                               @ModelAttribute("losingDeck") String losingDeck,
                               Model model) {

        String[] winners = new String[]{winningDeck}; //this allows for multi games in future for now handle 1v1
        String[] losers = new String[]{losingDeck};
        deckService.updateDeckRatings(winners, losers);

        model.addAttribute("decklist", deckService.getAllUniqueDecks());
        model.addAttribute("game", "NEED TO ADD GAME"); //todo fix this bit
        return "decklist";
    }

    @GetMapping(path="/deck/owner/{ownerId}")
    public String getPlayersDeck (@PathVariable("ownerId") String owner,
                                  Model model) {
        List<Deck> allUniqueDecks = deckService.getAllUniqueDecks();
        List<Deck> allUniquePlayerDecks = deckService.filterPlayersDecks(allUniqueDecks, owner);
        List<Deck> orderedDeckByRating = deckService.orderDeckByRating(allUniquePlayerDecks);
        model.addAttribute("title", owner +"'s Decks");
        model.addAttribute("pokemondecklist", deckService.filterByGame(orderedDeckByRating, "Pokemon"));
        model.addAttribute("ygodecklist", deckService.filterByGame(orderedDeckByRating, "Yu-Gi-Oh"));
        model.addAttribute("edhdecklist", deckService.filterByGame(orderedDeckByRating, "Commander"));

        return "ownersdecks";
    }

    @GetMapping(path="/deck/history/{deckId}")
    public String getDeckHistory (@PathVariable("deckId") String deckId,
                                  Model model ) {
        List<Deck> allDecks = deckService.getFullDeckList();
        List<Deck> deckHistoryList = deckService.getHistoryForDeck(allDecks, deckId);

        model.addAttribute("deckHistory", deckHistoryList);
        model.addAttribute("title", deckHistoryList.getFirst().getName() + " History \n Owner: " + deckHistoryList.getFirst().getOwner());

        //data for graphs
        model.addAttribute("chartData", deckService.getChartData(deckHistoryList));

        return "deckhistory";
    }

    @GetMapping(path="/decks/tier/{tier}/{game}")
    public String getDeckTiers (@PathVariable("tier") Integer tier,
                                @PathVariable("game") String game,
                                Model model) {
        List<Deck> allUniqueDecks = deckService.getAllUniqueDecks();
        List<Deck> filterByGame = deckService.filterByGame(allUniqueDecks, game);
        model.addAttribute("decklist", deckService.filterByTier(filterByGame, tier));
        model.addAttribute("title", game + " decks");
        model.addAttribute("game", game);

        return "decklist";
    }

}
