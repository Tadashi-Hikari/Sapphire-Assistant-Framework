# The Sapphire Assistant Framework
The Sapphire Assistant Framework is plug-in framework for [Athena](https://github.com/Tadashi-Hikari/Athena), an open source Google Assistant replacement (that doesn't require any Google services to run).

The Sapphire Framework is a data aggregation and processing framework for Android that allows users and developers to refine, customize, or develop a mobile assistant tailored to their needs.

## Highlights
* Free and Open Source
* Doesn't use Google services
* Works on Android 7.1 to 11
* Works entirely offline/on device
* Highly modular and extensible

## Motivation
The Sapphire Framework was developed to meet the needs of the open source community, while offering quality alternatives to producs in the closed source world. My original goal was simply to find a flexible mobile assistant that I could develop features for to help manage my ADHD. Finding nothing on the market to meet my need that also respected the ethics of free and open source software, I decided to start developing my own. It started out as a port of Mycroft for Android but due to system differences it quickly turned into its own project under the title "The Sapphire Assistant Framework". Due to concern that configuring the Sapphire Framework might overwhelm non-technical users, I split the code base in two to create [Athena](https://github.com/Tadashi-Hikari/Athena) and the Sapphire Framework, which can be installed stand alone or as a plug-in to Athena.


# How to Build The Sapphire Framework
It requires both Android platform 25 and 30 in order to build, and each individual module must be built and installed separately. The project was designed this way to test running 3rd party services in the background. If you are looking for something that works out of the box or is easier to build, I recommend looking at 
[Athena](https://github.com/Tadashi-Hikari/Athena)

# Build Status
Pre-Alpha 

# Frameworks and Libraries Used
* CMU PocketSphinx
* TensorSpeech TTS
* Stanford CoreNLP

# Contributions
* Financial contributions are always appreciated, and can help me move to making this my full time job. Check out the sponsor button in the repository if you are interested in helping to fund the project
* Join in the community on [Reddit](www.reddit.com/r/SapphireFramework) or Matrix at #SapphireFramework:matrix.org
* Android, Machine Learning developers, and UI/UX developers would be greatly appreciated for the project (or consulting at a minimum). I will continue to move forward reguardless, but it will go a lot quicker with some domain specific expertise
* Documentation help would be greatly appreciated. I generally have all of the information locked up in my head and I am working on documentation, but sometimes I miss what others would find helpful or need more information on. Feel free to ask questions on Reddit or Matrix (I'm pretty responsive) if you would like to help out
