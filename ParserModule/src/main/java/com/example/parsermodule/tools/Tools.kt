package com.example.parsermodule.tools

import java.io.File

class Tools {
    fun loadSentencesAndTags(){
        //request skills w/ data for training
        //Catch and aggrigate those files
        //Train from those aggrigated files
    }

    fun loadEntities(){

    }

    fun loadIntents(){

    }

    fun handleWildcards(){

    }

    fun REGEXIntent(){
        var regex = Regex("/{A-Z+,a-z+/}")
    }

    fun expandData(file: File){
        for(line in file){
            regex(line)
        }
    }
}