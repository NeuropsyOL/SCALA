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
The current version of SCALA contains a java library which detects artifacts and reconstucts data (jASR, find on branch jASR). Currently, the repository for the source code development is not yet public, it will be made public soon, though. The algorithm is closely based on the Artifact Subspace Reconstruction method [https://sccn.ucsd.edu/eeglab/plugins/ASR.pdf]. 
### DISCLAIMER: this library is considered an early beta, it contains at least one bug and is under development.
