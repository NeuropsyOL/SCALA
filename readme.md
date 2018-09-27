# SCALA

An Android application for the online signal processing and classification of time-series data. 
It has been developed at the University of Oldenburg during my PhD. 

## Overview of data flow
SCALA receives and sends out data using the LabstreamingLayer (https://github.com/edu.ucsd.sccn/labstreaminglayer).
A more detailed description of its workings and the setup will follow soon.


## Detailed description
Please refer to the publication for details of the usage and the setup required for an experiment: https://www.hindawi.com/journals/bmri/2017/3072870/.

If you have questions, please open an issue here on Github or drop me a mail.

## jASR
The current version of SCALA contain a java library which detects and reconstucts data (jASR, find on branch jASR). Currently, the repository for the source code development is not yet ublic, it will be made public soon, though. The algorithm is closely based on the Artifact Subspace Reconstruction method [https://sccn.ucsd.edu/eeglab/plugins/ASR.pdf]. 
### DISCLAIMER: this library is considered an early beta, it contains at least one bug and is under development.

## License

Copyright 2017,  Sarah Blum

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
