=Overview of Information Retrieval=
* obtain info relevant to need
==Query Suggestion==
suggest queries to users (query completion on google for ex)
* types of modification
    * modification - similar but not exact
    * expansion - make query more detailed/specific
    * deletion - make query more general
    * reformulation - substring replace
==Web Search==
exact match (DB-approach) not sufficient
* precison vs recall is application specific
==Question Answering==
auto answer natural language questions
==Recommendation Systems==
Pushes info to user, rather than user pulling via query
* Content-based - examine metadata
* Collaborative-filtering - examine historical data
  * Suffers from cold-start problem
* Hybrid

=Query Suggestion=
* Usually based on cooccurrence
  * Dice's Coefficient: \frac{n_{ab}}{n_a + n_b}
  * PMI \frac{p(a, b)}{p(a)p(b)} rank equiv \frac{n_{ab}}{n_a n_b}
    * Favors low frequency terms
  * Expected MI
  * Chi-Squared
  * Probably EMIM or Dice
* Pseudo-relevance feedback
  * Suggest based on top clicks on base query
* Query logs

=Web Search=
* Pick papers to present
* Traditional IR = early IR systems pre-Google-esque web search
