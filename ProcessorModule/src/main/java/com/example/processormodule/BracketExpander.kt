package com.example.processormodule

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

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
            for(sentence in sentences){
                parseExpression(sentence)
            }
        }catch(exception: Exception){
            Log.e("BracketExpander","There was an error with the intent!")
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // This is meant to be recursive
    fun parseExpression(sentence: String): List<String>{
        Log.i("BracketExapnder","Sentence to expand: ${sentence}")
        var sentenceList = mutableListOf<String>()
        var currentSentence = mutableListOf<String>()
        for(character in sentence.withIndex()){
            // The error is that it isn't advancing forward at all
            if(character.value == '('){
                // The length of the subexpression is defined in the recursion
                var subexpression = parseExpression(sentence.substring(character.index+1))
                // This is so that I don't break apart a bracket that isn't related to the skill.
                var normalBrackets = false
                if(subexpression.contains("|") == false){
                    normalBrackets = true
                    currentSentence.add("(")
                }
                currentSentence.addAll(subexpression)
                if(normalBrackets == true){
        }
            }else if(character.value == '|'){
                // I don't really understand the order here, but that's how it is in padatious, so.....
                // Begin parsing a new sentence
                Log.i("Bracket","what is cleared ${currentSentence}")
                currentSentence = mutableListOf()
                sentenceList.addAll(currentSentence)
            }else if(character.value == ')') {
                break
            }else{
                currentSentence.add(character.value.toString())
            }
        }
        Log.i("BracketExpander","Expanded sentences are: ${sentenceList}")
        return sentenceList
    }

    override fun onBind(intent: Intent?): IBinder?{
        TODO("Not yet implemented")
    }
}