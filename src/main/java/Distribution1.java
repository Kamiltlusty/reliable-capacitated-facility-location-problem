import ilog.concert.*;
import ilog.cplex.IloCplex;

public class Distribution1 {
    // sets
    private static final int i0 = 6;                    // liczba klientow (a..f)
    private static final int j0 = 5;                   // liczba regularnych facility (1..5)
    private static final int r0 = 5;                  // poziomy 0..4 regularne, 5 = emergency
    private static final int EMERGENCY_IDX = j0;       // indeks emergency facility = 5

    // parameters
    private static final double[] DEMAND = {15, 10, 15, 10, 15, 10};
    private static final double[][] SHIPPING_COST = {
            {10, 15, 10, 15, 10},
            {10, 15, 10, 15, 10},
            {10, 15, 10, 15, 10},
            {10, 15, 10, 15, 10},
            {10, 15, 10, 15, 10},
            {10, 15, 10, 15, 10}
    };
    private static final double[] CAPACITY = {20, 20, 20, 20, 20};
    private static final double V = 0.25;   // procent o jaki pojemnosc obiektow moze sie powiekszyc w razie awarii = 25%
    private static final int B = 3;  // max 3 obiekty moga przekroczyc swoja pojemnosc w razie awarii
    private static final double[] FAIL_PROB = {0.05, 0.02, 0.06, 0.04, 0.08, 0.00};

    // additional costs
    private static final double EMERGENCY_COST = 100.0;
    private static final double FIXED_COST = 1000.0;

    // decision variables
    private IloIntVar[] X;                      // czy obiekt jest otwarty
    private IloIntVar[][][] Y;                  // czy obiekt jest przypisany do klienta na danym poziomie
    private IloNumVar[][][] P;                  // prawdopodobienstwo ze obiekt j przypisany do klienta i, ma awarie
    private IloNumVar[] U;                      // czy zapotrzebowanie klienta przewyższa pojemnosc obiektu
    private IloNumVar[] Vj;                     // wielkosc przekroczenia pojemnosci
    private IloNumVar[][][] W;                  // linearyzacja P*Y: Prawdopodobieństwo, ze obiekt j obsluguje klienta i na poziomie r, wazone tym, czy w ogole jest do tego przypisane
    private IloCplex cplex;

    public Distribution1() throws IloException {
        cplex = new IloCplex();
        defineVariables();
        addConstraints();
        setObjective();
        if (cplex.solve()) {
            printSolution();
        } else {
            System.out.println("No solution found.");
        }
        cplex.end();
    }

    // initialization of decision variables
    private void defineVariables() throws IloException {
        // X
        X = new IloIntVar[j0 + 1];
        for (int j = 0; j < j0; j++) {
            X[j] = cplex.intVar(0, 1);
        }
        X[EMERGENCY_IDX] = cplex.intVar(1, 1);

        // Y
        Y = new IloIntVar[j0 + 1][i0][r0 + 1];
        for (int j = 0; j <= j0; j++) {
            for (int i = 0; i < i0; i++) {
                for (int r = 0; r <= r0; r++) {
                    Y[j][i][r] = cplex.intVar(0, 1);
                }
            }
        }

        // P i W
        P = new IloNumVar[j0 + 1][i0][r0 + 1];
        W = new IloNumVar[j0 + 1][i0][r0 + 1];
        for (int j = 0; j <= j0; j++) {
            for (int i = 0; i < i0; i++) {
                for (int r = 0; r <= r0; r++) {
                    P[j][i][r] = cplex.numVar(0, 1);
                    W[j][i][r] = cplex.numVar(0, 1);
                }
            }
        }

        // U i Vj
        U = new IloNumVar[j0 + 1];
        Vj = new IloNumVar[j0 + 1];
        for (int j = 0; j <= j0; j++) {
            U[j] = cplex.intVar(0, 1);
            Vj[j] = cplex.numVar(0, Double.MAX_VALUE);
        }
    }

    // constraints
    private void addConstraints() throws IloException {
        // (5) bierzemy albo zwykly obiekt albo emergency ale na kazdym poziomie klient musi miec przypisany jakis obiekt
        for (int i = 0; i < i0; i++) {
            for (int r = 0; r <= r0; r++) {
                IloLinearNumExpr sum = cplex.linearNumExpr();
                for (int j = 0; j < j0; j++) {
                    sum.addTerm(1.0, Y[j][i][r]);
                }
                for (int s = 0; s <= r; s++) {
                    sum.addTerm(1.0, Y[EMERGENCY_IDX][i][s]);
                }
                cplex.addEq(sum, 1.0);
            }
        }

        // (6) ograniczenie wymusza ze jesli obiekt jest przypisany do klienta to ten obiekt musi byc otwarty
        for (int i = 0; i < i0; i++) {
            for (int j = 0; j < j0; j++) {
                IloLinearNumExpr sum = cplex.linearNumExpr();
                for (int r = 0; r < r0; r++) {
                    sum.addTerm(1.0, Y[j][i][r]);
                }
                cplex.addLe(sum, X[j]);
            }
        }

        // (7) ograniczenie wymusza ze na poziomie 0 zapotrzebowanie nie moze byc wieksze niz pojemnosc
        for (int j = 0; j < j0; j++) {
            IloLinearNumExpr sum = cplex.linearNumExpr();
            for (int i = 0; i < i0; i++) {
                sum.addTerm(DEMAND[i], Y[j][i][0]);
            }
            cplex.addLe(sum, cplex.prod(CAPACITY[j], X[j]));
        }

        // (21) ograniczenie wymusza ze na pozostałych poziomach zapotrzebowanie nie moze byc wieksze niz pojemnosc powiększona o 25%, gdzie sume zwieksza prawdopodobienstwo awarii
        for (int j = 0; j < j0; j++) {
            IloLinearNumExpr sum = cplex.linearNumExpr();
            for (int i = 0; i < i0; i++) {
                for (int r = 1; r < r0; r++) {
                    sum.addTerm(DEMAND[i], W[j][i][r]);
                }
            }
            double factor = 1.0 / (1.0 - FAIL_PROB[j]);
            cplex.addLe(cplex.prod(factor, sum), cplex.sum(cplex.prod(CAPACITY[j], X[j]), Vj[j]));
        }

        // (9) ogranicza możliwość przekroczenia pojemności
        for (int j = 0; j < j0; j++) {
            cplex.addLe(Vj[j], cplex.prod(V * CAPACITY[j], U[j]));
        }

        // (10) maks 3 obiekty moga zwiekszyc pojemnosc
        IloLinearNumExpr sumU = cplex.linearNumExpr();
        for (int j = 0; j < j0; j++) {
            sumU.addTerm(1.0, U[j]);
        }
        cplex.addLe(sumU, B);

        // (11) istnieje dokladnie jeden poziom dla kazdego klienta, gdzie jest przypisany do emergency
        for (int i = 0; i < i0; i++) {
            IloLinearNumExpr sum = cplex.linearNumExpr();
            for (int r = 0; r <= r0; r++) {
                sum.addTerm(1.0, Y[EMERGENCY_IDX][i][r]);
            }
            cplex.addEq(sum, 1.0);
        }

        // (12) ustawia ze prawdopodobienstwo to 1 - ryzyko poszczegolnych obiektow
        for (int i = 0; i < i0; i++) {
            for (int j = 0; j <= j0; j++) {
                cplex.addEq(P[j][i][0], 1.0 - FAIL_PROB[j]);
            }
        }

        // (22) prawdopodobienstwo, że facility j bedzie obsługiwalo klienta i na poziomie r, uwzgledniajac awarie wszystkich facility na nizszych poziomach
        for (int i = 0; i < i0; i++) {
            for (int j = 0; j <= j0; j++) {
                for (int r = 1; r <= r0; r++) {
                    IloLinearNumExpr sum = cplex.linearNumExpr();
                    for (int k = 0; k < j0; k++) {
                        double coeff = FAIL_PROB[k] / (1.0 - FAIL_PROB[k]);
                        sum.addTerm(coeff, W[k][i][r - 1]);
                    }
                    cplex.addEq(P[j][i][r], cplex.prod(1.0 - FAIL_PROB[j], sum));
                }
            }
        }

        // (14)–(17) Linearyzacja: W = P * Y
        for (int i = 0; i < i0; i++) {
            for (int j = 0; j <= j0; j++) {
                for (int r = 0; r <= r0; r++) {
                    cplex.addLe(W[j][i][r], P[j][i][r]);
                    cplex.addLe(W[j][i][r], Y[j][i][r]);
                    cplex.addGe(W[j][i][r], 0.0);
                    cplex.addGe(W[j][i][r], cplex.sum(Y[j][i][r], cplex.sum(P[j][i][r], -1.0)));
                }
            }
        }
    }

    // objective function
    private void setObjective() throws IloException {
        IloLinearNumExpr objective = cplex.linearNumExpr();

        // (2)
        for (int i = 0; i < i0; i++) {
            for (int j = 0; j < j0; j++) {
                for (int r = 0; r < r0; r++) {
                    objective.addTerm(DEMAND[i] * SHIPPING_COST[i][j], W[j][i][r]);
                }
            }
        }

        // (3)
        for (int i = 0; i < i0; i++) {
            for (int r = 0; r <= r0; r++) {
                objective.addTerm(DEMAND[i] * EMERGENCY_COST, W[EMERGENCY_IDX][i][r]);
            }
        }

        // (4)
        for (int j = 0; j < j0; j++) {
            objective.addTerm(FIXED_COST, X[j]);
        }

        cplex.addMinimize(objective);
    }

    private void printSolution() throws IloException {
        System.out.println("Objective value = " + cplex.getObjValue());
        System.out.print("Opened facilities: ");
        for (int j = 0; j < j0; j++) {
            if (cplex.getValue(X[j]) > 0.9) {
                System.out.print((j + 1) + " ");
            }
        }
        System.out.println();
    }

    public static void main(String[] args) {
        try {
            new Distribution1();
        } catch (IloException e) {
            e.printStackTrace();
        }
    }
}