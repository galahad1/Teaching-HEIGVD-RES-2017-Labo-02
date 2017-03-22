# Lab 2: TCP Programming

## Table of Contents

1. [Introduction](#Introduction)
2. [Objectives](#Objectives)
3. [Evaluation](#Evaluation)
4. [Specifications](#Specifications)
6. [Tasks](#Tasks)
    1. [Task 1: Study the specification and the code provided for the server](#Task1)
    1. [Task 2: Execute the server and play with it](#Task2)
    1. [Task 3: Implement the client for the Roulette Protocol v1](#Task3)
    1. [Task 4: Write automated tests to specify the behaviour of the v2 protocol](#Task4)
    1. [Task 5: Extend the server to implement the Roulette Protocol V2](#Task5)


## <a name="Deadlines"></a>Deadlines

* Check the deadlines on Cyberlearn. 
* As always, they are very strict.
* Be aware that there is an intermediate deadline! If you do not deliver your automated tests in time, your grade will be impacted.

## <a name="Important"></a>IMPORTANT

* When you create the `.java` files that contain your JUnit tests, make sure to include one of your GitHub ids in the name (replace 'wasadigi' with your GitHub id in `RouletteV1WasadigiTest.java`). This is important to make sure that all groups create their own files, so that we don't have merge conflicts afterwards.

## <a name="Introduction"></a>Introduction

The goal of this lab is to **get familiar with TCP programming in Java**, by working both with a server and a client. We will consider a **custom application-level protocol**, the "Quiz Roulette" protocol, which is used to write sadistic client-server applications. More on this later.

[![](images/roulette.gif)](http://www.imdb.com/title/tt0077416/)

**For this lab, you can work in pairs**, so that you can discuss while you go through the tasks. Please make use of your two computers. Run the client on one computer and the server on the other. Validate that you are able to communicate across the network.


## <a name="Objectives"></a>Objectives

After this lab, you should be able to:

* **Use the telnet command from a terminal to connect to a TCP server** and act as a *human client*. In other words, you should be able to type and send commands to the server and see the responses on the console.

* **Write a TCP client in Java**, using the Socket class. You should be able to explain the relationship that exists between sockets, input and output streams. You should be able to explain how sockets are identified (describe the 4 attributes) and explain where and how this information is found in TCP segments.

* **Write a TCP server in Java**, using the ServerSocket and Socket classes. You should be able to describe the overall structure of the program. In other words, you should be able to describe what classes need to be implemented, what are their responsibilities and how they interact with each other.

* **Write a multi-threaded TCP server in Java**, using the Runnable interface and the Thread class. You should be able to describe how the notion of **worker** is a useful abstraction to describe what is happening within a server. You should be able to describe that at least two types of workers are found in network servers. You should be able to **describe the role of these workers by making an analogy** to a real-world situation (e.g. what happens in a restaurant or in a supermarket).

* **Write automated tests** with the JUnit framework to define the expected behavior of your client-server application.

* **Use Wireshark to analyze the flow of data** exchanged by your TCP server and client.



## <a name="Evaluation"></a>Evaluation

* There is **a grade** for this lab. You can work in groups of 2 students max. 
* **No report to write**.

## <a name="Specifications"></a>Specifications

* Your goal is firstly to understand the implementation of a custom application-level protocol (the Roulette Protocol), by reading the specification and analyzing the provided code. Your goal is then to extend the provided code, by implementing the version 2 of the Roulette Protocol.

### The Roulette Protocol V1

* **The Roulette Protocol is a session-oriented (stateful) protocol**. This means that clients establish a connection, send any number of commands to the server, process the responses and finally send a command to terminate the session with a special command (defined in the protocol). 

* The Roulette Protocol uses **TCP** as the underlying transport protocol. The **default port is 1313**.

* **A client session can be in two different states**. In the initial state, the server accepts the following commands: `HELP`, `RANDOM`, `LOAD`, `INFO` and `BYE`. When the client sends the `LOAD` command, the session enters into the other state. In this state, the server reads client data line by line (and interprets each line as the full name of a student to add to the data store) until the `ENDOFDATA` is sent by the client. When this is the case, the session state goes back to the initial state.


* **The protocol defines the following commands**, which are sent by the clients and processed by the servers as follows:

Command | Processing done by the server       | Response               
:---:|--------|---
`HELP`    | The server retrieves the commands defined by the protocol version. | `Commands: [HELP, RANDOM, LOAD, INFO, BYE]`
`RANDOM`    | The server randomly selects one of the students in its store. | `{"fullname":"olivier liechti"}`, where `olivier liechti` is the name of the victim.
`LOAD`    | The server changes the state of the session and starts reading client data line by line until it gets a line with the `ENDOFDATA` string. Every new line is interpreted as the full name of a new student that is added to the data store.  | After receiving the `LOAD` command immediately returns `Send your data [end with ENDOFDATA]`. After receiving `ENDOFDATA`, the server sends back `DATA LOADED`. 
`INFO`    | The server retrieves the protocol version and the number of students currently in the store. | `{"protocolVersion":"1.0","numberOfStudents":3}`, where `3` is the number of students currently in the server data store
`BYE`    | The server closes the connection.  | *No response*


### The Roulette Protocol V2

The version 2 of the Roulette Protocol specifies minor modifications, as follows:

* the default port is 2613

* the protocol defines the following **additional commands**:

Command | Processing done by the server       | Response               
:---:|--------|---
`CLEAR`    | The server resets the data store by clearing all students | `DATASTORE CLEARED`
`LIST`    | The server fetches the list of students in the store | `{"students":[{"fullname":"john doe"},{"fullname":"bill smith"}]}`, where the value of students is an array containing all students in the store.

* the protocol **modifies** the following **existing commands** defined in version 1:

Command | Processing done by the server       | Response               
:---:|--------|---
`LOAD`    | No change in the processing. | `{"status":"success","numberOfNewStudents":3}`, where 3 is the number of student lines sent by the client.
`BYE`    | No change in the processing. | `{"status":"success","numberOfCommands":12}`, where 12 is the number of commands sent by the client during the session.
`INFO`    | No change in the processing. | `{"protocolVersion":"2.0","numberOfStudents":3}`, where `3` is the number of students currently in the server data store.


## <a name="Tasks"></a>Tasks

### <a name="Task1"></a>Task 1: Study the specification and the code provided for the server

The first step of the lab is for you to **read  the [specifications](#Specifications) of the Roulette Protocol very carefully**. Make sure that you understand what the client can send to the server after being connected, how the server should react and what the client should receive back.

When this is clear, **go through the code** of the [QuizRouletteServer project](QuizRouletteServer). Assuming that you are familiar with the material presented in class and with the various examples, you should be able to grasp the role of the different classes and methods quite easily. You should be able to make the connection between the specification and the code.

**If that is not the case, go back to this material and do some studying. It is not worth trying to progress blindly, otherwise…**

![](images/progressing-blindly.gif)

### <a name="Task2"></a>Task 2: Execute the server and play with it

Note that when you build the whole project (`code` + `tests`), you will initially have a failure. The reason is there are tests for the client, which you still need to implement (so you will get UnsupporteOperationExceptions). Nevertheless, the server code is successfully built! This means that you can go in the `target` folder of the `code` project and find the executable jar.

Execute the server program and make sure that it starts as expected and that it listens on the specified port. Open a terminal window and connect to the server by typing:

```
telnet localhost 1313
```

Once connected, remember that **you are acting as the client**. Hence, it is up to you to send commands by typing them in the terminal window (followed by return). Send commands and validate that you observe is in line with your understanding of the protocol specification. Try to send incorrect commands as well. Also, make sure to you use the `LOAD` command and to create several students.

After your first test, **open several terminal windows** and open several connections in parallel. This will allow you to validate that the server is able to server multiple clients in parallel. Try to load data from the different clients and validate that they **see the same server data**.

Have a look in the [QuizRouletteClient](QuizRouletteClient) folder, read the code in the [client.js](QuizRouletteClient/client.js) file and type the following command and make sure that you understand what is happening:

```
node client.js
```

Finally, **run Wireshark and capture traffic** between your clients and the server. **Inspect the TCP segments** and study the content on the header fields (source address, source port, destination address, destination port). Compare these values for outgoing and incoming traffic. Make sure that you understand the relationship between these header values and how sockets are identified on both sides of the communication channel.

### <a name="Task3"></a>Task 3: Implement the client for the Roulette Protocol v1

**Implement the RouletteV1ClientImpl.java class**, based on the specification in this document and on the provided unit tests. Be aware, however, that these tests do not fully validate the behaviour of your code!

Thanks to your implementation, it should be possible to write a client application for the Roulette Protocol (e.g. an application doing the same job as the `client.js` Node.js script provided in the lab repo).

### <a name="Task4"></a>Task 4: Write automated tests to specify the behaviour of the v2 protocol

Each group should write a collection of JUnit tests **before** implementing the v2 Roulette client and server. The tests are meant to specify the expected behavior of the application, so that it can be validated automatically, on a regular basis.

Create a file `RouletteV2XXXTest.java` (replace XXX with your GitHub account) in the test project and add at least 3 tests to validate other aspects of the code. In this test file, create methods to validate the v2 specification as completely as possible. In other words, read carefully the Roulette v2 Protocol Specification and define a test for every rule defined in the protocol (port, message syntax, behaviour).

When all groups have submitted their tests, we will integrate them on the master branch of the original repo. All groups will then have to merge the contributions into their repo. In other words, each group will validate its implementation against the tests submitted by all groups (and we will add our own).

To do that, each group will need to setup git in order to **work in a feature branch**. When you have implemented your tests, you should **submit a pull request**. **We only want test code in this pull request, and no application code!**

```
# Use this command to create the feature branch and use it
$ git fetch origin
$ git checkout fb-lab02-v2-tests
$ git checkout -b fb-lab02-v2-contrib

# Use git add and git commit as usual
...

# Use this command to push the feature branch to your fork
$ git push origin fb-lab02-v2-contrib
```

### <a name="Task5"></a>Task 5: implement the Roulette Protocol V2 (client and server)

The last task of the lab is for you to **extend the server project** and to **add an implementation of the version 2** of the Roulette protocol. In other words, when you are done the server should be able to work both in V1 and in V2 modes.

Make sure that you have understood the difference between the two protocol versions. Analyze what is being asked and specifcy how you are going to implement it. It will essentially be done by storing some state in new variables and by serializing content in server responses. **This should be a piece of cake!**

![](images/cake.gif)

trigger
