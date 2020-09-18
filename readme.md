# SCALA - Signal ProCessing And CLassification on Android

SCALA is an Android application which can be used to realize Brain-Computer Interfaces on smartphones.

![Main Screen, 20%](/docs/SCALA_main.jpg)


## Overview of data flow
### Input of data into SCALA
SCALA receives and sends out data using the LabstreamingLayer (https://github.com/labstreaminglayer).
The app will look for any streams in the network with type 'EEG' and can receive data from this stream. Additionally, it will resolve any marker streams to receive event markers. If LSL is not an option for your markers, SCALA can also receive UDP markers at port 50006 (and sends out response markers at port 50008). 

### Processing of data
Since SCALA is a BCI application, it will receive data all the time, but it will only **process** these data, once a relevant marker has been received. The marker content can be set in the settings, currently SCALA implements a two-class classifier and therefore, only two marker messages can be specified.
Once a relevant marker for one of the classes has been received, SCALA stores a segment of data in an internal buffer. The length of this data segment can be specified in the settings, the default is 3 seconds. Once the data buffer is filled with data, SCALA will process this data.

The processing is currently tailored to one specific experiment outlined in https://www.hindawi.com/journals/bmri/2017/3072870/. This paper also contains a more detailed description of SCALA's architecture. 

Please contact me if you want to adapt SCALA for your own use case!