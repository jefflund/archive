genpass
=======

XKCD style passphrase generator. See http://xkcd.com/936/ for the original inspiration.
It generates passwords for each site you care about given a particular seed. You may
wonder why I don't just use something like the excellent LastPass service. The answer
is that I hope to have these passphrases memorized, but do not want to have to rely
exclusively upon my own memory.

seed
====
If you use this for generating passwords you actually use, make sure to keep your seed
a secret, as it can be used to generate all your other passwords. Choose a seed which
cannot be guessed easily (e.g. your birthday or low numbers are probably not good
choices). You might also want to keep your genpass usage out of your bash history.
Granted, if someone else has access to your bash history, they are root and you are
already screwed, but if you are paranoid about your coworkers/sysadmins, consider
adding `*genpass*` to your `HISTIGNORE`.

usage
=====
```
genpass [-h] [--phrase-length PHRASE_LENGTH]
        [--min-word-length MIN_WORD_LENGTH]
        [--max-word-length MAX_WORD_LENGTH] [--word-file WORD_FILE]
        [--entropy]
        [sites] [seed]

positional arguments:
  sites                 List of passwords to generate
  seed                  Seed for the random number generator

optional arguments:
  -h, --help            show this help message and exit
  --phrase-length PHRASE_LENGTH, -p PHRASE_LENGTH
                        Number of words in the passphrases
  --min-word-length MIN_WORD_LENGTH, -w MIN_WORD_LENGTH
                        Minimum length of words in the passphrases
  --max-word-length MAX_WORD_LENGTH, -W MAX_WORD_LENGTH
                        Maximum length of words in the passphrases
  --word-file WORD_FILE
                        List of words to use in the passphrases
  --entropy, -e         Print entropy statistics
```

examples
========
```
./genpass sites.txt 123456789
google: millotherscentwatch
aml: hourburyfeeldry
lds: permitcouchdivineliquid
router: rimchefseaokay
wellsfargo: dosesellyieW10
byu: designreststupiddrawB3
reuse: issuecellritualman
```

sites
=====

The sites file is simply a newline separated list of sites for which you want
passwords to be generated. Note that appending new sites to a list will not
change previous passwords, but reordering or inserting sites in the middle of
this list will change the generated passwords.

Some sites have ridiculous requirements, so you can indicate that a site is
constrained by adding a `#` to the end of site name. Doing so will append
the first letter of the site in caps, and add the length of the site name as
a number. For example, the site name `byu#` results in a password of the form
`designreststupiddrawB3`. Adding a number after the pound indicates that the
site is also constrained in length. For example, the name `wellsfargo#14`
indicates that the password should be truncated to be no longer than 14
characters. It will still have the postfix, so a password for the above site
would have the form `dosesellyieW10`.
