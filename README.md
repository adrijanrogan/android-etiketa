# etiketa

Etiketa je Android aplikacija, ki omogoča spreminjanje metapodatkov MP3 in FLAC glasbenih datotek.
* Podpora za ID3v1, ID3v2 (MP3) in VorbisComment (FLAC)
* Spreminjanje naslova, albuma, izvajalca in leta izdaje
* Brskanje po celotnem uporabniškem datotečnem sistemu
* Dark/Night tema

## Načrtovane spremembe in izboljšave

* Uporaba storage access framework (uporaba File v prihodnje ne bo več možna)
* Možnost spremembe slike (artwork)
* 'Single Activity' design
* Deljenje informacij preko ViewModel (npr. izbrana datoteka)


## Uporabniški vmesnik
<table><tr>
<td> <img src="screens/1_file_browser.png" alt="File Browser" style="width: 256px;"/> </td>
<td> <img src="screens/2_file_editor.png" alt="File Editor" style="width: 256px;"/> </td>
<td> <img src="screens/3_file_sorting.png" alt="File Sorting" style="width: 256px;"/> </td>
<td> <img src="screens/4_file_information.png" alt="File Information" style="width: 256px;"/> </td>
<td> <img src="screens/5_dark_theme.png" alt="Dark Theme" style="width: 256px;"/> </td>
</tr></table>

## O projektu

Projekt je nastal v okviru projektne naloge pri informatiki na splošni maturi z naslovom "Uporaba JNI v razvoju Android aplikacije". Končna verzija je bila napisana v Javi, projekt pa se je od takrat prepisal v Kotlinu in se še vedno razvija.

Dostop do metapodatkov poteka preko knjižnice [TagLib](https://taglib.org/), okoli katere je napisan JNI wrapper ([1](https://github.com/adrijanrogan/etiketa/tree/master/app/src/main/java/com/github/adrijanrogan/etiketa/jni) in [2](https://github.com/adrijanrogan/etiketa/tree/master/app/src/main/cpp/taglib/jni)).
