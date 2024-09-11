package com.trials.deck_tiering.controller;

import com.trials.deck_tiering.model.Deck;
import com.trials.deck_tiering.service.DeckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.List;

@Controller
public class DeckListController {

    @Autowired
    private DeckService deckService;

    @RequestMapping("/")
    public String getDeckList(Model model) {
        List<Deck> allUniqueDecks = deckService.getAllUniqueDecks();
        model.addAttribute("decklist", deckService.orderDeckByRating(allUniqueDecks));
        return "decklist";
    }

    @RequestMapping(path="/deck/winners={winners}&losers={losers}")
    public String updateResults(@PathVariable("winners") String winnerIds,
                                @PathVariable("losers") String losersIds,
                                Model model) {

        String[] winners = winnerIds.split(","); //this allows for multi games in future for now handle 1v1
        String[] losers = losersIds.split(",");

        deckService.updateDeckRatings(winners, losers);

        model.addAttribute("decklist", deckService.getAllUniqueDecks());
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
        return "decklist";
    }

    @GetMapping(path="/deck/{ownerId}")
    public String getPlayersDeck (@PathVariable("ownerId") String owner,
                                  Model model) {
        List<Deck> allUniqueDecks = deckService.getAllUniqueDecks();
        List<Deck> allUniquePlayerDecks = deckService.filterPlayersDecks(allUniqueDecks, owner);
        model.addAttribute("owner", owner +"'s Decks");
        model.addAttribute("decklist", deckService.orderDeckByRating(allUniquePlayerDecks));
        return "ownersdecks";
    }
}
