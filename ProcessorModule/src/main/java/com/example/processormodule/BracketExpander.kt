package com.example.processormodule

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlin.math.exp

/**
 * This is fairly directly copied from Mycrofts Padatatious parser. Why reinvent the wheel?
 * Padatious skills: {bracket} indicates an entity, {bracket} could be an existing entity, or a wildcard.
 * (A | B) indicates OR. It could be recursive, and it can have a blank space " " in the B position
 * which indicates that it can be their or not. # is a number wildcard matcher. There is some code
 * to ignore brackets without the pipe/bar in the sentence. As far as I can tell, this is so things like
 * phone numbers (###) ###-#### can be properly entered
 */

class BracketExpander: Service(){

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i("BracketExpander","BracketExpander intent received")
        try {
            var sentences = intent.getStringArrayListExtra("BRACKET_TEST")!!
            parseSentenceList(sentences)
        }catch(exception: Exception){
            Log.e("BracketExpander","There was an error with the intent!")
            Log.e("BracketExpander",exception.toString())
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // Recursion is probably still important
    fun parseSentenceList(listOfStrings: ArrayList<String>) {
        for (sentence in listOfStrings) {
            // Add them all to the list
            if(sentence.contains("(")) {
                var expandedSentences = mutableListOf<String>()
                // This has to start at the '(' Otherwise it gets called wrong. Too many loop throughs
                var start = sentence.indexOf('(')
                // Might be able to repalace w/ substringAfter()
                expandedSentences.addAll(expandBrackets(sentence.substring(start)))
                for (incompleteSentence in expandedSentences) {
                    Log.i("BracketExpander", "Final sentence: ${sentence.substringBefore('(')}${incompleteSentence.trim()} ${sentence.substringAfterLast(')')}")
                }
            }
        }
    }

    // I am assuming it starts in a bracket '('
    fun expandBrackets(bracketedSentence: String): MutableList<String>{
        // set index to 1, to avoid the starting bracket
        var word = ""; var fragment = "";  var index = 1
        // This is supposed to hold all total sentences
        var allSentences = mutableListOf<String>()
        // This is supposed to hold the sentences for this level
        var sentenceFragments = mutableListOf<String>()


        while(index < bracketedSentence.length){
            //Log.i("BracketExpander","checking character ${bracketedSentence.get(index)}")
            // Avoid the first '(' and keep moving through each character
            // If another bracket is found, start again with a whole new substring. (recursively)
            if(bracketedSentence.get(index)  == '('){
                // might be able to replace w/ substringAfter()
                var returnedSentences = expandBrackets(bracketedSentence.substring(index))
                //Log.i("BracketExpander","Returned sentences: ${returnedSentences}")
                //sentenceFragments.addAll(returnedSentences)

                // This is the segment I just added. may need to delete
                for (sentence in returnedSentences){
                    // This adds the prefix properly, but there's a duplication somewhere
                    sentenceFragments.add(fragment+sentence)
                    // because I just added prefix fragment, I am clearing it out
                    fragment=""
                }

                // The bracket is the first one encountered AFTER the one that initiated the top expandBracket()
                // changing this to 1 (adding 2 totall caught large, and addon. They're out of order though
                var bracketCount = 1; var indexAdvance = 1
                // adding one to index gets good woahs, but misses living....?
                for(character in bracketedSentence.substring(index+1)) {
                    if(character == '('){
                        bracketCount++
                    }else if(character == ')'){
                        bracketCount--
                    }

                    if(bracketCount == 0){
                        // I need ')' to trigger its closeout, so move it back just one
                        // without the -1 one it skips 2+ brackets. Why...
                        index+=indexAdvance
                        //Log.i("BracketExpander","Moving to index ${index}, character ${bracketedSentence.get(index+1)}")
                        break
                    }
                    indexAdvance++
                }
            }else if(bracketedSentence.get(index) == '|'){
                // Save whatever was behind it
                // This is the part I am currently adding in
                if(sentenceFragments.size != 0){
                    for (sentence in sentenceFragments){
                        // This is adding the postfix fragment to formed sentences
                        //Log.i("BracketExpander","For | adding: ${sentence+fragment} to allSentences")
                        allSentences.add(sentence + fragment)
                    }
                }else{
                    //Log.i("BracketExpander","For | adding: ${fragment} to allSentences")
                    allSentences.add(fragment)
                }
                // Start a new sentence
                fragment = ""
                // I think I was forgetting to clear the list sentenceFragments. This was causing a redundant addon
                sentenceFragments = mutableListOf()
            // Can this be merged to the code above?
            }else if(bracketedSentence.get(index) == ')'){
                // Sentence fragments are expected to have just been returned.
                if(sentenceFragments.size != 0) {
                    for (sentence in sentenceFragments) {
                        // Log.i("BracketExpander","For ) adding: ${fragment+sentence} to allSentences")
                        // This is adding the prefixed fragments to all recently returned sentences
                        allSentences.add(fragment+sentence)
                    }
                }else{
                    //Log.i("BracketExpander","For ) adding: ${fragment} to allSentences")
                    allSentences.add(fragment)
                }
                //That is it for this subexpression
                break
            }else{
                // Get the current character
                fragment+=bracketedSentence.get(index)
                //Log.i("BracketExpander","adding letter ${bracketedSentence.get(index)}")
            }
            index++
        }
        // All expanded sentences (within | a bracket)
        //Log.i("BracketExpander","returning ${allSentences}")
        return allSentences
    }

    override fun onBind(intent: Intent?): IBinder?{
        TODO("Not yet implemented")
    }
}