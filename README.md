# Tubes1_muggle IF 2211 Strategi Algoritma
> Pemanfaatan Algoritma Greedy dalam Aplikasi Permainan “GALAXIO”

## KELOMPOK MUGGLE
13518134 Raihan Iqbal
13521020 Varraz Hazandra Abrar
13521026 Kartini Copa

## Table of Contents
* [Deskripsi Umum](#deskripsi-umum)
* [Requirement dan Instalasi](#trequirement-dan-instalasi)
* [Build](#build)
* [Cara Menjalankan Program](#cara-menjalankan-program)
* [Cara menjalankan program](#cara-menjalankan-program)


## Deskripsi Umum
Galaxio adalah sebuah game battle royale yang mempertandingkan bot kapal anda dengan beberapa bot kapal yang lain. Setiap pemain akan memiliki sebuah bot kapal dan tujuan dari permainan adalah agar bot kapal anda yang tetap hidup hingga akhir permainan. Penjelasan lebih lanjut mengenai aturan permainan akan dijelaskan di bawah. Agar dapat memenangkan pertandingan, setiap bot harus mengimplementasikan strategi tertentu untuk dapat memenangkan permainan

<img width="523" alt="image" src="https://user-images.githubusercontent.com/102657926/219686670-7c182454-5465-4188-9eea-ee7135a57d4c.png">


## Requirement dan Instalasi
Windows
Linux
MacOS
NET Core 3.1


## Build
Command: mvn clean package


## Cara menjalankan game
1. Konfigurasi jumlah bot yang ingin dimainkan pada file JSON ”appsettings.json” dalam folder “runner-publish” dan “engine-publish”
2. Buka terminal baru pada folder runner-publish.
3. Jalankan runner menggunakan perintah “dotnet GameRunner.dll”
4. Buka terminal baru pada folder engine-publish
5. Jalankan engine menggunakan perintah “dotnet Engine.dll”
6. Buka terminal baru pada folder logger-publish
7. Jalankan engine menggunakan perintah “dotnet Logger.dll”
8. Jalankan seluruh bot yang ingin dimainkan
9. Setelah permainan selesai, riwayat permainan akan tersimpan pada 2 file JSON “GameStateLog_{Timestamp}” dalam folder “logger-publish”.
