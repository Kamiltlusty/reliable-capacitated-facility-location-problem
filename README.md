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