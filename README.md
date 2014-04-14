passgen
=======

XKCD style passphrase generator. See http://xkcd.com/936/ for the original inspiration.

Usage
=====
```
./genpass [--phrase-length PHRASE_LENGTH]
        [--min-word-length MIN_WORD_LENGTH]
        [--max-word-length MAX_WORD_LENGTH]
        [--num-passphrases NUM_PASSPHRASES]
        [--word-file WORD_FILE]
        [--separator SEPARATOR]
        [--entropy]

optional arguments:
  -h, --help            show this help message and exit
  --phrase-length PHRASE_LENGTH, -p PHRASE_LENGTH
                        Number of words in the passphrases
  --min-word-length MIN_WORD_LENGTH, -w MIN_WORD_LENGTH
                        Minimum length of words in the passphrases
  --max-word-length MAX_WORD_LENGTH, -W MAX_WORD_LENGTH
                        Maximum length of words in the passphrases
  --num-passphrases NUM_PASSPHRASES, -n NUM_PASSPHRASES
                        Number of passphrases to generate
  --word-file WORD_FILE
                        List of words to use in the passphrases
  --separator SEPARATOR, -s SEPARATOR
                        Separator between words in the passphrase
  --entropy, -e         Print entropy statistics

```
Examples
========
```
./genpass
smartthumbradiationadministrative
```
```
./genpass -n 5
satisfactioncentralscareconceive
opportunityfeministphysicsstair
wholeblankreasonabledramatic
biologicalbesidesundermineheadquarters
badlysafelystrangerclear
```
```
./genpass -e -w 5 -W 6 -n 5 -s " "
~42 bits of entropy
~113 years at 1000 guesses/sec
amount remove almost budget
legal virus drama verbal
basis cancer tumor vanish
formal stick magic league
safety drawer and/or upset
```
