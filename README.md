# SHACL validator plugin

This repository is used to build the plugin needed to complete the implementation of the SHACL shape validator
(see https://citnet.tech.ec.europa.eu/CITnet/stash/projects/ITB/repos/docker-validator-shacl/browse).

The generated plugin JAR needs to be copied to the SHACL validator's configuration and its configuration file
adapted as follows:
```
validator.plugins.extended.0.class = eu.europa.ec.itb.shacl.plugin.ValidationServiceAdvancedFeatureShaclPlugin
validator.plugins.extended.0.jar = PATH_TO/shacl-shacl-1.0-SNAPSHOT-jar-with-dependencies.jar
validator.plugins.extended_best_practices.0.class = eu.europa.ec.itb.shacl.plugin.ValidationServiceBestPracticesShaclPlugin
validator.plugins.extended_best_practices.0.jar = PATH_TO/shacl-shacl-1.0-SNAPSHOT-jar-with-dependencies.jar
```