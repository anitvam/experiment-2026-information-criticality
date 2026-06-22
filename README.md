# Self-Adaptive Information Sharing in Heterogeneous Vehicular Multi-Agent Systems

Sharing information among Autonomous Vehicles can help to enhance Situational Awareness, 
but practical issues including computational limits, 
communication bandwidth, 
privacy requirements, 
and heterogeneous sensing capabilities preven unrestricted data exchange. 

We propose a framework for information sharing in heterogeneous Multi-Agent Systems,
allowing vehicles to selectively share information according to importance and spatial context, 
while taking privacy and communication constraints into account. 

This experiment is a prototype showing how spatial propagation of shared information can
maximize information utility 
and minimize unnecessary communication,
thereby improving Situational Awareness in dynamic environments.

### Execute the graphical simulation

1. Install a Gradle-compatible version of Java.
  Use the [Gradle/Java compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html)
  to learn which is the compatible version range.
  The Version of Gradle used in this experiment can be found in the `gradle-wrapper.properties` file
  located in the `gradle/wrapper` folder.
2. Install the version of Python indicated in `.python-version` (or use `pyenv`).
3. Launch either:
    - `./gradlew runGradientGraphic` on Linux, MacOS, or Windows if a bash-compatible shell is available;
    - `gradlew.bat runGradientGraphic` on Windows cmd or Powershell;
5. After the simulation has loaded, press "P" on the keyboard to start it.

