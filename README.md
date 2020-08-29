# Controllable AF
This project generates 3 main commands: 
### theoryGen 
This command generates the 2 agents theory using the generation.config file that must be in the same directory  
Usage: java -jar theoryGen.jar -g -o output [-s seed]  
example : java -jar theoryGen.jar -g -s 0 -o theory.theory  
theory.TheoryCommand is the main class of this command (under src/theory/TheoryCommand.java)

### cafGen 
This command generates the 2 agents caf using the generation.config file that must be in the same directory  
Usage: java -jar cafGen.jar -g -i theoryInputFile [-s seed]  
example: java -jar cafGen.jar -g -i theory.theory -s 0  
caf.cafCommand is the main class of this command (under src/caf/cafCommand.java)  

### ControllableAF
This command launches the discussion between the 2 agents using the previously generated cafs  
Usage: java -jar ControllableAF.jar  
Main class: Agents.main  

To generate theses 3 commands under Intellij-idea click on Build -> Build Artifacts -> all artifacts you will find the 3 commands under the "out/artifacts" directory
