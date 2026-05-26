# reliable-capacitated-facility-location-problem

Problem definition summary:
- one staged supply network
- customers demand is well forecasted and order quantities are known
- cost to ship a unit of demand from facility j to customer i, denoted by $ d_{ij} $ is deterministic and values are known
- facilities are at risk of failing due to disruptions
- failure probability of facilities is site-dependent and values are known
- $ f_j $ are the fixed location cost
- $ 0 < q_j < 1 $ are the probability of failure
- events of facility disruptions are independent
- each facility has a specific throughput capacity to supply customers demands 
- capacity limit of the facilities is strictly observed when no failures
- operative facilities are allowed a slight exceed of capacities by $ v_j $ when failure
- maximum amount that facilities can exceed normal capacity is specified
- $ \phi_i $ is a cost associated with each customer i $ \in I $. It is a penalty for not serving the customer per unit of missed demand. May be accrued
  even if some of the assigned facilities remain operational, when $ \phi_i < $ cost of serving i through any of these facilities.
- $ j = j_0 is the "emergency" facility introduced for such scenario.
- $ f_{j_0} = 0 $ - a fixed cost of emergency facility. 
- $ q_{j_0} = 0 $ - failure probability of emergency facility
- $ d_{ij_0} = \phi_i $ - transportation cost to the customer of emergency facility
- $ r_0 $ is the number of facilities assigned to customer
- $ r_0-1 $ is the last regular facility
- At optimality every customer denoted as i is expected to have precisely $ r_0 $ assignments unless assigned to the emergency facility at certain levels s < r0  
- If customer i is allocated to exactly $ r_0 $ regular facilities spanning levels 0 through r_0 − 1, must
  additionally be assigned to the emergency facility j0 at level r0. It is scenario where all regular facilities potentially fail
- The objective is to minimize the total fixed costs of setting up facilities and the cost
  of transportation, in addition to the expected cost of disruptions, which includes the
  expected transportation and unsatisfied demands penalty costs.

Numerical example 1:

|            | Shipping costs |    |    |    |    |    |
|------------|----------------|----|----|----|----|----|
|            | Candidate locations |    |    |    |    |    |
| Customers  | 1  | 2  | 3  | 4  | 5  | Demand |
|------------|----|----|----|----|----|--------|
| a | 10 | 15 | 10 | 15 | 10 | 15 |
| b | 10 | 15 | 10 | 15 | 10 | 10 |
| c | 10 | 15 | 10 | 15 | 10 | 15 |
| d | 10 | 15 | 10 | 15 | 10 | 10 |
| e | 10 | 15 | 10 | 15 | 10 | 15 |
| f | 10 | 15 | 10 | 15 | 10 | 10 |
| Capacity | 20 | 20 | 20 | 20 | 20 |
| Failure probability | 0.05 | 0.02 | 0.06 | 0.04 | 0.08 |  |
| Emergency cost | 100 | 100 | 100 | 100 | 100 |  |
| Fixed location cost | 1000 | 1000 | 1000 | 1000 | 1000 |  |

# Implementacja modelu CRFLP (z pracy  Mathematical formulations and a Relax-and-Fix heuristic algorithm for capacitated reliable fixed-charge facility location problems ~ Abdolreza Roshani1 · Glenn Parry2 · Philip Walker-Davies3)

## Zmienne decyzyjne (zgodne z sekcją 3.3)

- $ X_j \in \{0,1\} $ – czy otwarte (dla $ j = 0..j_0 $, przy czym $ X_{j_0}=1 $ zawsze)
- $ Y_{ijr} \in \{0,1\} $ – czy facility $ j $ przypisane do klienta $ i $ na poziomie $ r $
- $ U_j \in \{0,1\} $ – czy oczekiwane obciążenie przekracza pojemność
- $ V_j \ge 0 $ – wielkość przekroczenia
- $ P_{ijr} \in [0,1] $ – prawdopodobieństwo (w linearyzacji)
- $ W_{ijr} $ – zmienna pomocnicza: $ W_{ijr} = P_{ijr} Y_{ijr} $

## Ograniczenia

W funkcji `addConstraints()` nadawane są ogarniczenia:

| Oznaczenie | Równanie                                                                                         | Krótki opis                                                                                                                                                          |
|------------|--------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| (5) | $$ \sum_{j=0}^{j_0-1} Y_{ijr} + \sum_{s=0}^{r} Y_{i j_0 s} = 1 $$                                | bierzemy albo zwykly obiekt albo emergency ale na kazdym poziomie klient musi miec przypisany jakis obiekt                                                           |
| (6) | $$ \sum_{r=0}^{r_0-1} Y_{ijr} \le X_j $$                                                         | ograniczenie wymusza ze jesli obiekt jest przypisany do klienta to ten obiekt musi byc otwarty                                                                       |
| (7) | $$ \sum_{i=1}^{I} h_i Y_{ij0} \le c_j X_j $$                                                     | ograniczenie wymusza ze na poziomie 0 zapotrzebowanie nie moze byc wieksze niz pojemnosc                                                                             |
| (21) | $$ \frac{1}{1-q_j} \sum_{i} h_i \sum_{r=1}^{r_0-1} W_{ijr} \le c_j X_j + V_j $$                  | ograniczenie wymusza ze na pozostałych poziomach zapotrzebowanie nie moze byc wieksze niz pojemnosc powiększona o 25%, gdzie sume zwieksza prawdopodobienstwo awarii |
| (9) | $$ V_j \le v\,  U_j $$                                                                           | ogranicza możliwość przekroczenia pojemności                                                                                                                         |
| (10) | $$ \sum_{j=0}^{j_0-1} U_j \le b $$                                                               | maks 3 obiekty moga zwiekszyc pojemnosc                                                                                                                              |
| (11) | $$ \sum_{r=0}^{r_0} Y_{i j_0 r} = 1 $$                                                           | istnieje dokladnie jeden poziom dla kazdego klienta, gdzie jest przypisany do emergency                                                                              |
| (12) | $$ P_{ij0} = 1 - q_j $$                                                                          | ustawia ze prawdopodobienstwo to 1 - ryzyko poszczegolnych obiektow                                                                                                  |
| (22) | $$ P_{ijr} = (1-q_j) \sum_{k=0}^{j_0-1} \frac{q_k}{1-q_k} W_{i k\, r-1} $$                       | prawdopodobienstwo, że facility j bedzie obsługiwalo klienta i na poziomie r, uwzgledniajac awarie wszystkich facility na nizszych poziomach                         |
| (14)–(17) | $ W_{ijr} \le P_{ijr},\; W_{ijr} \le Y_{ijr},\; W_{ijr} \ge 0,\; W_{ijr} \ge Y_{ijr}+P_{ijr}-1 $ | linearyzacja                                                                                                                                                         |

## Funkcja celu

W `setObjective()` sumowane są składniki:

całkowity przewidywany koszt transportacji
$$ C_T = \sum_{i=0}^{i_0-1} \sum_{j=0}^{j_0-1} \sum_{r=0}^{r_0-1} h_i d_{ij} W_{ijr} $$
całkowity przewidywany koszt awarii
$$ C_L = \sum_{i=0}^{i_0-1} \sum_{r=0}^{r_0} h_i \phi_i W_{i j_0 r} $$
koszty stałe 
$$ C_F = \sum_{j=0}^{j_0-1} f_j X_j $$

Minimalizowane jest $ C_T + C_L + C_F $.