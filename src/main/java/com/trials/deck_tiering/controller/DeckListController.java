package com.trials.deck_tiering.controller;

import com.trials.deck_tiering.model.Deck;
import com.trials.deck_tiering.model.GameEnum;
import com.trials.deck_tiering.service.DeckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.List;

import static com.trials.deck_tiering.model.GameEnum.getGameList;

@Controller
public class DeckListController {

    @Autowired
    private DeckService deckService;

    @RequestMapping("/")
    public String index(Model model) {
        model.addAttribute("gameList", getGameList());
        return "index.html";
    }

    @GetMapping("/decks/game/{game}")
    public String getDeckList(@PathVariable("game") String game,
                              Model model) {
        List<Deck> allUniqueDecks = deckService.getAllUniqueDecks();
        List<Deck> filteredByGame = deckService.filterByGame(deckService.orderDeckByRating(allUniqueDecks), game);

        model.addAttribute("decklist", filteredByGame);
        model.addAttribute("title", game + " decks");
        model.addAttribute("game", game);
        return "decklist";
    }

    @PostMapping(path="/deck/winners={winners}&losers={losers}") //unused
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

    @RequestMapping(path="/deck/addoutcome/game/{game}")
    public String createOutcomeForGame(@PathVariable("game") String game,
                                       Model model) {
        List<Deck> allUniqueDecks = deckService.getAllUniqueDecks();
        List<Deck> filteredByGame = deckService.filterByGame(deckService.orderDeckByRating(allUniqueDecks), game);
        model.addAttribute("decklist", filteredByGame);
        model.addAttribute("game", game);
        return "addoutcome";
    }

    @PostMapping(path="/deck/resultsupdate/game/{game}")
    public String updateResult(@ModelAttribute("winningDeck") String winningDeck,
                               @ModelAttribute("losingDeck") String losingDeck,
                               @PathVariable String game,
                               Model model) {

        if(winningDeck.isEmpty() || losingDeck.isEmpty()) { //prevent error HTTP 500 on forgetting to add result
            return "index";
        }

        String[] winners = new String[]{winningDeck}; //this allows for multi games in future for now handle 1v1
        String[] losers = new String[]{losingDeck};
        deckService.updateDeckRatings(winners, losers);

        List<Deck> allUniqueDecks = deckService.getAllUniqueDecks();
        List<Deck> filteredByGame = deckService.filterByGame(deckService.orderDeckByRating(allUniqueDecks), game);

        model.addAttribute("decklist", filteredByGame);
        model.addAttribute("game", game); //todo fix this bit
        return "decklist";
    }

    @PostMapping("/decks/add")
    public String addDeck(@ModelAttribute("deckName") String deckName,
                          @ModelAttribute("ownerName") String owner,
                          @ModelAttribute("gameName") String game,
                          Model model) {

        //add to csv the new deck
        return "decklist";
    }

    @GetMapping("/decks/new")
    public String newDeck(Model model) {

        model.addAttribute("gameList", getGameList());

        return "newdeck";
    }

    @GetMapping(path="/deck/owner/{ownerId}")
    public String getPlayersDeck (@PathVariable("ownerId") String owner,
                                  Model model) {
        List<Deck> allUniqueDecks = deckService.getAllUniqueDecks();
        List<Deck> allUniquePlayerDecks = deckService.filterPlayersDecks(allUniqueDecks, owner);
        List<Deck> orderedDeckByRating = deckService.orderDeckByRating(allUniquePlayerDecks);
        model.addAttribute("title", owner +"'s Decks");
        model.addAttribute("pokemondecklist", deckService.filterByGame(orderedDeckByRating, GameEnum.POKEMON.getName()));
        model.addAttribute("ygodecklist", deckService.filterByGame(orderedDeckByRating, GameEnum.YUGIOH.getName()));
        model.addAttribute("edhdecklist", deckService.filterByGame(orderedDeckByRating, GameEnum.COMMANDER.getName()));
        model.addAttribute("magicdecklist", deckService.filterByGame(orderedDeckByRating, GameEnum.MAGIC.getName())); //todo implement

        return "ownersdecks";
    }

    @GetMapping(path="/deck/history/{deckId}")
    public String getDeckHistory (@PathVariable("deckId") String deckId,
                                  Model model ) {
        List<Deck> allDecks = deckService.getFullDeckList();
        List<Deck> deckHistoryList = deckService.getHistoryForDeck(allDecks, deckId);

        //idk whats going on here and why it won't let me do in single line
        String string = "Rating: " + deckHistoryList.getLast().getRating();
        String string2 = " Tier: " + String.valueOf(deckHistoryList.getLast().getTier());

        model.addAttribute("deckHistory", deckHistoryList);
        model.addAttribute("title", deckHistoryList.getFirst().getName() + "  Owner: " + deckHistoryList.getFirst().getOwner());
        model.addAttribute("currentRating", string + string2);
        //TODO ADD OUTPUT FOR HISTORY VS OPPONENTS

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
//TODO add controller for adding multiple games in a row

//TODO - other functionalities to add
//        showing history of games played for all/specific games
//        showing history of a decks games played
//        show who a deck is best against or worst against
//        player coefficient?
//        player rating
//        card list for a deck
//        add a deck api