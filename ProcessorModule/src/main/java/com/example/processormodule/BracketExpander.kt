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
                expandedSentences.addAll(expandBrackets(sentence.substring(start)))
                for (incompleteSentence in expandedSentences) {
                    Log.i("BracketExpander", "Final sentence: ${sentence.substringBefore('(')}${incompleteSentence.trim()} ${sentence.substringAfterLast(')')}")
                }
            }
        }
    }

    fun expandSentence(sentence: String){

    }

    // I am assuming it starts in a bracket '('
    fun expandBrackets(bracketedSentence: String): MutableList<String>{
        var word = ""; var sentence = "";  var index = 0
        var sentences = mutableListOf<String>();

        // I need to
        while(index < bracketedSentence.length){
            // Avoid the first '(' and keep moving through each character
            index++
            // If another bracket is found, start again with a whole new substring. (recursively)
            if(bracketedSentence.get(index)  == '('){
                var fragments = expandBrackets(bracketedSentence.substring(index))
                for(fragment in fragments){
                    sentences.add(sentence+fragment)
                }
                // Keep processing after the index of the substring that was just processed
                index = bracketedSentence.indexOf(")")
            }else if(bracketedSentence.get(index) == '|'){
                // Save whatever was behind it
                sentences.add(sentence)
                // Start a new sentence
                sentence = ""
            }else if(bracketedSentence.get(index) == ')'){
                // This is where the 'a' glitch is coming from. It's adding the postfix.
                // This also might be where the empty sentences are coming from
                sentences.add(sentence)
                //That is it for this subexpression
                break
            }else{
                // Get the current character
                sentence+=bracketedSentence.get(index)
            }
        }
        // All expanded sentences (within | a bracket)
        return sentences
    }

    override fun onBind(intent: Intent?): IBinder?{
        TODO("Not yet implemented")
    }
}