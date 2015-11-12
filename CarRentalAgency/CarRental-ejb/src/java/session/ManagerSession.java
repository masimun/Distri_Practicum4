package session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import rental.Car;
import rental.CarRentalCompany;
import rental.CarType;
import rental.DataLoader;
import rental.Reservation;

@Stateless
public class ManagerSession implements ManagerSessionRemote {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Set<String> getAllRentalCompanies() {
        try {
            Set<String> companies = new HashSet(
                    (List<String>) em.createNamedQuery(
                            "rental.CarRentalCompany.getAllCarRentalCompanyNames")
                    .getResultList());

            return companies;
        } catch (Exception e) {
            throw new EJBException(e);
        }

    }

    @Override
    public List<CarType> getCarTypes(String company) {
        try {
            return new ArrayList<CarType>(
                    (List<CarType>) em.createNamedQuery(
                            "rental.CarRentalCompany.getAllCarTypesByCompany").setParameter("company", company)
                    .getResultList());
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    @Override
    public int getNumberOfReservations(String companyName, String type) {
        try {
            List resultList = em.createNamedQuery(
                    "rental.CarRentalCompany.getNumberOfReservationsForCarTypeInCompany")
                    .setParameter("companyName", companyName)
                    .setParameter("carType", type)
                    .getResultList();

            return Integer.valueOf(resultList.get(0).toString());

        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

    @Override
    public int getNumberOfReservationsDoneBy(String renter) {
        try {
            return Integer.valueOf(em.createNamedQuery(
                    "rental.CarRentalCompany.getNumberOfReservationsByRenter")
                    .setParameter("renter", renter)
                    .getSingleResult().toString());

        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

    @Override
    public List<String> getBestClients() {
        try {
            List<Object[]> resList = em.createNamedQuery(
                    "rental.CarRentalCompany.getClientReservationList")
                    .getResultList();

            List<String> bestClients = new ArrayList();
            //get the maximum number of reservations
            long maxRes = (long) resList.get(0)[1];

            for (Object[] row : resList) {
                if (((long) row[1]) >= maxRes) {
                    bestClients.add((String) row[0]);
                } else {
                    return bestClients;
                }

            }
            return bestClients;

        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

    @Override
    public CarType getMostPopularCarType(String company) {
        try {
            return (CarType) em.createNamedQuery(
                    "rental.CarRentalCompany.getMostPopularCarTypeByCompany")
                    .setParameter("company", company)
                    .getResultList().get(0);

        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

    @Override
    public Set<Integer> getCarIds(String company, String type) {
        try {
            Set<Integer> carIds = new HashSet<Integer>(em.createNamedQuery(
                    "rental.CarRentalCompany.getCarIds")
                    .setParameter("company", company)
                    .setParameter("type", type)
                    .getResultList());
            return carIds;

        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

    @Override
    public void addCarType(String name, int nbOfSeats,
            float trunkSpace, double rentalPricePerDay, boolean smokingAllowed) {
        CarType ct = new CarType();
        em.persist(ct);
    }

    @Override
    public void addCar(String Company, int amount, CarType ct) {
        CarRentalCompany crc = (CarRentalCompany) em.find(CarRentalCompany.class, Company);
        for (int i = 1; i < amount; i++) {
            /*
             the cars automatically presists the carTypes (Cascading)
             */
            crc.addCar(new Car(ct));
        }
    }

    @Override
    public void addCarRentalComapany(String compName) {
        ArrayList<CarType> addedCarTypes = new ArrayList<>();
        ArrayList<Car> cars = new ArrayList<>();
        try {
            for (Entry<CarType, Integer> e : DataLoader.loadData(compName + ".csv").entrySet()) {
                CarType t = e.getKey();
                for (int i = 0; i < e.getValue(); i++) {
                    cars.add(new Car(t));
                }
            }

        } catch (Exception ex) {

        }
        CarRentalCompany comp = new CarRentalCompany(compName, cars);

        /*
         the car retnal company automatically presists the cars,
         the cars automatically presists the carTypes (Cascading)
         */
        em.persist(comp);

    }

    @Override
    public String getMostPopularCarRentalCompany() {
        try {
            return (String) em.createNamedQuery(
                    "rental.CarRentalCompany.getMostPopularCarRentalCompanies")
                    .getResultList().get(0);

                    
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            throw new EJBException(e);
        }
            
    }

}
