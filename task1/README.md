# Explore FoundationDB and its Java Client Library

## Install and setup FoundationDB server (version 6.2.30) up and running:

- I downloaded the appropriate package for my OS from the official FoundationDB downloads page: [FoundationDB Downloads](https://apple.github.io/foundationdb/downloads.html).
- I used GitHub Collab to run this project, I chose the Linux/Ubuntu version and downloaded it with the following commands:
    ```bash
  wget https://github.com/apple/foundationdb/releases/download/6.3.23/foundationdb-clients_6.3.23-1_amd64.deb
  wget https://github.com/apple/foundationdb/releases/download/6.3.23/foundationdb-server_6.3.23-1_amd64.deb
  ```
- I installed both the server and client packages using the dpkg command:
    ```bash
    sudo dpkg -i foundationdb-clients_6.3.23-1_amd64.deb
    sudo dpkg -i foundationdb-server_6.3.23-1_amd64.deb
    ```
- To ensure that the installation was successful, I ran the fdbcli command and checked the status in the FoundationDB terminal.
    ```bash
    fdbcli
    status
    ```
- I successfully installed and set up the FoundationDB server (version 6.2.30) and its client on an Ubuntu system.
- This task was completely, and FoundationDB server and client were installed without any issues, and the server was verified to be running correctly.
- There were no obstacles encountered during the installation and setup process.

## Install Java 8, Maven, and IntelliJ:

- I already had Java 8, and Maven installed in my MacOS, I just downloaded and installed IntelliJ from Internet.
- This task was completely by installing Java 8, Maven, and IntelliJ, and no obstacles encountered during the installation process.

## Use the FDB command line tool (fdbcli) to connect to the cluster. Try to use its commands set,get, getrange and explore other commands (via command help in fdbcli)

- First I opened FDB command line tool using fdbcli.
- I tried out few of the below commands:
    ```bash
    writemode on 
    set k1 v1
    set k2 v2
    set k3 v3
    writemode off
    get k1
    getrange k1 k4 5
    writemode on
    clear k3
    clearrange k1 k3
    begin
    set k1 v1
    set k2 v2
    commit
    getversion
    begin
    set k3 v4
    reset
    set k3 v3
    commit
    begin
    set k4 v4
    rollback
    exit
    ```
