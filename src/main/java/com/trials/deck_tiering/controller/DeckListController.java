package com.trials.deck_tiering.controller;

import com.trials.deck_tiering.model.Deck;
import com.trials.deck_tiering.model.GameEnum;
import com.trials.deck_tiering.service.DeckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.List;

import static com.trials.deck_tiering.model.GameEnum.*;

@Controller
public class DeckListController {

    @Autowired
    private DeckService deckService;

    @PostMapping(path="/deck/resultsupdate/game/{game}")
    public String updateResult(@RequestParam("deckA") String deckAId,
                               @RequestParam("deckB") String deckBId,
                               @RequestParam("winsA") Integer winsA,
                               @RequestParam("winsB") Integer winsB,
                               @PathVariable String game,
                               Model model) {

        if (deckAId == null || deckBId == null || deckAId.isBlank() || deckBId.isBlank()) {
            getDecksbyGameUnfiltered(model, game);
            model.addAttribute("error", "Please select two decks.");
            return "addoutcome";
        }

        if (deckAId.equals(deckBId)) {
            getDecksbyGameUnfiltered(model, game);
            model.addAttribute("error", "Please select two different decks.");
            return "addoutcome";
        }

        if (winsA == null || winsB == null || winsA < 0 || winsB < 0) {
            getDecksbyGameUnfiltered(model, game);
            model.addAttribute("error", "Wins must be numbers 0 or greater.");
            return "addoutcome";
        }

        if (winsA.equals(winsB)) {
            getDecksbyGameUnfiltered(model, game);
            model.addAttribute("error", "Draws aren't supported — please enter a winning score.");
            return "addoutcome";
        }

        String winnerId;
        String loserId;
        int winnerWins;
        int loserWins;

        if (winsA > winsB) {
            winnerId = deckAId;
            loserId = deckBId;
            winnerWins = winsA;
            loserWins = winsB;
        } else {
            winnerId = deckBId;
            loserId = deckAId;
            winnerWins = winsB;
            loserWins = winsA;
        }

        deckService.updateDeckRatings1v1(winnerId, loserId, winnerWins, loserWins);

        getDecksbyGame(model, game);
        return "decklist";
    }

    private void getDecksbyGame(Model model, String game) {
        List<Deck> allUniqueDecks = deckService.getAllUniqueDecks();
        List<Deck> filteredByGame = deckService.filterByGame(deckService.orderDeckByRating(allUniqueDecks), game);

        model.addAttribute("decklist", filteredByGame);
        model.addAttribute("game", game);
    }


    private void getDecksbyGameUnfiltered(Model model, String game) {
        List<Deck> allUniqueDecks = deckService.getAllUniqueDecksUnfiltered();
        List<Deck> filteredByGame = deckService.filterByGame(deckService.orderDeckByRating(allUniqueDecks), game);

        model.addAttribute("decklist", filteredByGame);
        model.addAttribute("game", game);
    }

    @RequestMapping("/")
    public String index(Model model) {


        model.addAttribute("ygoList", getYugiohGameList());
        model.addAttribute("pokemonList", getPokemonGameList());
        model.addAttribute("mtgList", getMtgGameList());

        return "index.html";
    }

    @GetMapping("/decks/game/{game}")
    public String getDeckList(@PathVariable("game") String game,
                              Model model) {
        getDecksbyGame(model, game);
        model.addAttribute("title", game + " decks");
        return "decklist";
    }

    @RequestMapping(path="/deck/addoutcome/game/{game}")
    public String createOutcomeForGame(@PathVariable("game") String game,
                                       Model model) {
        getDecksbyGameUnfiltered(model, game);
        if( game.equals(YUGIOHTEAM.getName())) { //for multi games
            return "addoutcomemulti";
        }
        if(game.equals(YUGIOHBR.getName()) ||
                game.equals(COMMANDER.getName())) { //for ffa games
            return "addoutcomeffa";
        }
        return "addoutcome";
    }


//    @PostMapping(path="/deck/resultsupdate/multi/game/{game}")
//    public String updateResultMulti(@ModelAttribute("winningDeck1") String winningDeck1,
//                               @ModelAttribute("losingDeck1") String losingDeck1,
//                               @ModelAttribute("winningDeck2") String winningDeck2,
//                               @ModelAttribute("losingDeck2") String losingDeck2,
//                               @PathVariable String game,
//                               Model model) {
//
//        if(winningDeck1.isEmpty() || losingDeck1.isEmpty() || losingDeck2.isEmpty()) { //prevent error HTTP 500 on forgetting to add result
//            return "index";
//        }
//
//        String[] winners = new String[]{winningDeck1, winningDeck2}; //this allows for multi games in future for now handle 1v1
//        String[] losers = new String[]{losingDeck1, losingDeck2};
//        deckService.updateDeckRatings(winners, losers, false);
//
//        getDecksbyGame(model, game);
//        return "decklist";
//    }

//    @PostMapping(path="/deck/resultsupdate/multi/game/{game}/bestof3")
//    public String updateResultMultiBestof3(@ModelAttribute("winningDeck1") String winningDeck1,
//                                    @ModelAttribute("losingDeck1") String losingDeck1,
//                                    @ModelAttribute("winningDeck2") String winningDeck2,
//                                    @ModelAttribute("losingDeck2") String losingDeck2,
//                                    @PathVariable String game,
//                                    Model model) {
//
//        if(winningDeck1.isEmpty() || losingDeck1.isEmpty() || losingDeck2.isEmpty()) { //prevent error HTTP 500 on forgetting to add result
//            return "index";
//        }
//
//        String[] winners = new String[]{winningDeck1, winningDeck2}; //this allows for multi games in future for now handle 1v1
//        String[] losers = new String[]{losingDeck1, losingDeck2};
//        deckService.updateDeckRatings(winners, losers, true);
//
//        getDecksbyGame(model, game);
//        return "decklist";
//    }


//    @PostMapping(path="/deck/resultsupdate/ffa/game/{game}")
//    public String updateResultFFA(@ModelAttribute("winningDeck1") String winningDeck1,
//                               @ModelAttribute("losingDeck1") String losingDeck1,
//                               @ModelAttribute("losingDeck3") String losingDeck3,
//                               @ModelAttribute("losingDeck2") String losingDeck2,
//                               @PathVariable String game,
//                               Model model) {
//
//        if(winningDeck1.isEmpty() || losingDeck1.isEmpty() || losingDeck2.isEmpty()) { //prevent error HTTP 500 on forgetting to add result
//            return "index";
//        }
//
//        String[] winners = new String[]{winningDeck1}; //this allows for multi games in future for now handle 1v1
//        String[] losers;
//
//        if(losingDeck3.isEmpty()) {
//            losers = new String[]{losingDeck1, losingDeck2};
//        }
//        else {
//            losers = new String[]{losingDeck1, losingDeck2, losingDeck3};
//        }
//
//        deckService.updateDeckRatings(winners, losers, false);
//        getDecksbyGame(model, game);
//        return "decklist";
//    }

//    @PostMapping(path="/deck/resultsupdate/ffa/game/{game}/bestof3")
//    public String updateResultFFABestof3(@ModelAttribute("winningDeck1") String winningDeck1,
//                                  @ModelAttribute("losingDeck1") String losingDeck1,
//                                  @ModelAttribute("losingDeck3") String losingDeck3,
//                                  @ModelAttribute("losingDeck2") String losingDeck2,
//                                  @PathVariable String game,
//                                  Model model) {
//
//        if(winningDeck1.isEmpty() || losingDeck1.isEmpty() || losingDeck2.isEmpty()) { //prevent error HTTP 500 on forgetting to add result
//            return "index";
//        }
//
//        String[] winners = new String[]{winningDeck1}; //this allows for multi games in future for now handle 1v1
//        String[] losers;
//
//        if(losingDeck3.isEmpty()) {
//            losers = new String[]{losingDeck1, losingDeck2};
//        }
//        else {
//            losers = new String[]{losingDeck1, losingDeck2, losingDeck3};
//        }
//
//        deckService.updateDeckRatings(winners, losers, true);
//        getDecksbyGame(model, game);
//        return "decklist";
//    }

    @PostMapping("/decks/add")
    public String addDeck(@ModelAttribute Deck deck,
                          Model model) {

        deckService.addNewDeck(deck);
        String game = deck.getGame();

        getDecksbyGame(model, game);

        return "decklist";
    }

    @GetMapping("/decks/new")
    public String newDeck(Model model) {

        model.addAttribute("gameList", getMainGameList());
        model.addAttribute("deck", new Deck());
        model.addAttribute("title", "Add A New Deck");

        return "newdeck";
    }

//    @GetMapping("/decks/{deckid}/decklist/{cardlist}")
//    public String getCardlist(@PathVariable("deckid") String deckId, //this isn't working currently from the template
//                              @PathVariable("cardlist") String cardList,
//                              Model model) {
//        List<Card> listofCards;
//        try {
//            listofCards = deckService.getCardList(cardList); //this eventually will be an object
//        } catch (IOException e) {
//            return "cardList";
//        }
//
//        List<Card> mainDeck = listofCards.stream().filter(card -> card.getDeckLocation().equals("Deck")).toList();
//
//        //below are only used in YuGiOh
//        List<Card> extraDeck = listofCards.stream().filter(card -> card.getDeckLocation().equals("Extra Deck")).toList();
//        List<Card> sideDeck = listofCards.stream().filter(card -> card.getDeckLocation().equals("Side Deck")).toList();
//
//        if(extraDeck.isEmpty()){
//            model.addAttribute("gameCheck", false);
//        }
//        else {
//            model.addAttribute("gameCheck", true);
//        }
//
//        model.addAttribute("title", "Deck List");
//        model.addAttribute("mainDeck", mainDeck); //split by extra, regular and side
//        model.addAttribute("extraDeck", extraDeck); //split by extra, regular and side
//        model.addAttribute("sideDeck", sideDeck); //split by extra, regular and side
//        model.addAttribute("deckId", deckId);
//
//        return "cardlist";
//    }

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
        model.addAttribute("ygobrdecklist", deckService.filterByGame(orderedDeckByRating, GameEnum.YUGIOHBR.getName()));
        model.addAttribute("ygogendecklist", deckService.filterByGame(orderedDeckByRating, GameEnum.YUGIOHGENESYS.getName()));
        model.addAttribute("ygoteamdecklist", deckService.filterByGame(orderedDeckByRating, GameEnum.YUGIOHTEAM.getName()));
        model.addAttribute("magicdecklist", deckService.filterByGame(orderedDeckByRating, GameEnum.MAGIC.getName())); //todo implement

        return "ownersdecks";
    }

    @GetMapping(path="/deck/history/{deckId}")
    public String getDeckHistory(@PathVariable("deckId") String deckId,
                                 Model model) {
        List<Deck> allDecks = deckService.getFullDeckList();
        List<Deck> deckHistoryList = deckService.getHistoryForDeck(allDecks, deckId);
        List<String> deckHistoryOutput = deckService.getHistoryOutput(deckId);

        deckHistoryList = deckService.addHistoryOutput(deckHistoryList, deckHistoryOutput);

        String rating = "Rating: " + deckHistoryList.getLast().getRating();
        String tier = " Tier: " + deckHistoryList.getLast().getTier();
        String matches = " Matches: " + deckHistoryList.getLast().getGamesPlayed();

        model.addAttribute("deckHistory", deckHistoryList);
        model.addAttribute("title", deckHistoryList.getFirst().getName() + "  Owner: " + deckHistoryList.getFirst().getOwner());
        model.addAttribute("currentRating", rating + tier + matches);
        model.addAttribute("deckId", deckHistoryList.getFirst().getId());
        model.addAttribute("cardList", deckHistoryList.getFirst().getCardList());
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
        List<Deck> filterByGame = deckService.filterByGame(deckService.orderDeckByRating(allUniqueDecks), game);
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
//        card list improvements
//              better card insight, way to add decks easier, visual for the cards/deck?
//        have decks have all their playstyles grouped together