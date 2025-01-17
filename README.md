# Introduction

The current repository holds the resources needed to build a [custom validator plugin](https://www.itb.ec.europa.eu/docs/guides/latest/creatingCustomValidatorPlugin/index.html) for the [SHACL shape validator](https://www.itb.ec.europa.eu/shacl/shacl/upload) provided by the [Interoperability Test Bed](https://interoperable-europe.ec.europa.eu/collection/interoperability-test-bed-repository/solution/interoperability-test-bed). The configuration repository for this validator is available [here](https://github.com/ISAITB/validator-resources-shacl).

# Building the plugin

To build the plugin you need:

* A Java Development Kit (v11+)
* Apache Maven (v3.8.0+)

The generated plugin JAR needs to be copied to the SHACL validator's configuration and its configuration file
adapted as follows:
```
validator.plugins.extended.0.class = eu.europa.ec.itb.shacl.plugin.ValidationServiceAdvancedFeatureShaclPlugin
validator.plugins.extended.0.jar = PATH_TO/shacl-shacl-1.0-SNAPSHOT-jar-with-dependencies.jar
validator.plugins.extended_best_practices.0.class = eu.europa.ec.itb.shacl.plugin.ValidationServiceBestPracticesShaclPlugin
validator.plugins.extended_best_practices.0.jar = PATH_TO/shacl-shacl-1.0-SNAPSHOT-jar-with-dependencies.jar
```

# Licence

This plugin is shared using the [European Union Public Licence (EUPL) version 1.2](https://interoperable-europe.ec.europa.eu/sites/default/files/custom-page/attachment/eupl_v1.2_en.pdf).

# Legal notice

The authors of this repository and the resulting plugin waive any and all liability linked to its usage or the interpretation of its results. In terms of data, the resulting plugin does not harvest, collect or process in any way data that could be linked to the user or
workstation.

# Contact

To get in touch for feedback or questions please send an email to [DIGIT-ITB@ec.europa.eu](mailto:DIGIT-ITB@ec.europa.eu).