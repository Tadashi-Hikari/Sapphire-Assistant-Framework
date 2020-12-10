package com.example.parsermodule.tools

import java.io.File

/**
 * The purpose of this file is to provide essential tools/functions to making an app work with the
 * assistant framework. I believe what I am putting here is functions that will handle the Uris sent
 * to the parser module for training
 */

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

    fun expandData(file: File) {
        /**
        for(line in file){
        regex(line)
        }
        }
         **/
    }
}