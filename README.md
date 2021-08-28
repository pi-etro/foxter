<p align="center">
  <a href="https://github.com/pi-etro/foxter">
    <img src="https://raw.githubusercontent.com/pi-etro/foxter/main/img/foxter.svg" width="400">
  </a>
</p>
<p align="center">
    <a href="https://www.java.com" alt="Made with Java">
        <img src="https://img.shields.io/badge/Made%20with-Java-B07219.svg" /></a>
    <a href="https://www.gnu.org/licenses/gpl-3.0.html" alt="GPLv3">
        <img src="https://img.shields.io/badge/License-GPLv3-CB0000.svg" /></a>
</p>

<div align="center">
  P2P CLI program for large file transfers
</div>


## Table of Contents

* [Usage](#usage)
* [System design](#system-design)
* [About](#about)
* [License](#license)

## Usage

Download the source code [here](https://github.com/pi-etro/foxter/archive/main.zip) and compile it with JDK 8:

```bash
javac Servidor.java Peer.java
```

Launch the server on one terminal with `java Servidor` and enter the IP `127.0.0.1`. Once the server is online, peers can join the network to register their ip, port and files and to search for files on other peers.

Launch the peer with `java Peer`, enter the IP `127.0.0.1`, a port number and the absolute path of the folder with the files to be shared.

After the peer has joined and searched for a file present on other peer, the option to download the file directly from this peer is enabled.

To leave the network, the peer can select the `LEAVE` option or close the terminal (the server automatically detects that this peer is now offline and clears its data).

## System design

The server (Servidor) has two main tasks: handle various peer requests and responses and periodically check if each peer is online (Alive).

<p align="center">
    <img src="https://raw.githubusercontent.com/pi-etro/foxter/main/img/server.svg" alt="Server Design" width="500">
</p>

The peer has three tasks: handle multiple peers download requisitions, handle Alive control messages or responses from the server and execute the selected menu tasks (`JOIN`, `SEARCH`, `DOWNLOAD` or `LEAVE`).

<p align="center">
    <img src="https://raw.githubusercontent.com/pi-etro/foxter/main/img/peer.svg" alt="Server Design" width="500">
</p>

This system can transfer large files (up to GB files were tested) between peers, this was achieved by sending small 1 KB "packages" until EOF (end-of-file).

## About

This project was developed for the UFABC Distributed Systems course. Each student implemented a simplified version of [Napster](https://en.wikipedia.org/wiki/Napster_(pay_service)).

**DISCLAIMER:** No 🦊 were injured during the development of this program.

## License
[GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.html)

