package rental;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

    
    @NamedQueries({
    @NamedQuery(name="rental.CarRentalCompany.getNumberOfReservationsForCarTypeInCompany",
                query="SELECT COUNT(res) FROM Reservation res WHERE res.rentalCompany = LOWER(:companyName) AND res.carType = :carType"),
    @NamedQuery(name="rental.CarRentalCompany.getNumberOfReservationsByRenter",
                query="SELECT COUNT(res) FROM Reservation res WHERE res.carRenter= :renter"),  
    @NamedQuery(name="rental.CarRentalCompany.getClientReservationList",
                query="SELECT res.carRenter,COUNT(res.carRenter) FROM Reservation res GROUP BY res.carRenter ORDER BY COUNT(res.carRenter) DESC"),
    @NamedQuery(name="rental.CarRentalCompany.getMostPopularCarTypeByCompany",
                query="SELECT ct FROM Reservation res JOIN CarType ct WHERE res.rentalCompany = LOWER(:company) GROUP BY ct ORDER BY COUNT(res.rentalCompany) DESC"),
    @NamedQuery(name="rental.CarRentalCompany.getCarIds",                           
                query="SELECT c.id FROM CarRentalCompany comp JOIN comp.cars c WHERE comp.name = :company AND c.type.name = :type"),
    @NamedQuery(name="rental.CarRentalCompany.getAllCarRentalCompanyNames",                           
                query="SELECT comp.name FROM CarRentalCompany comp"),
    @NamedQuery(name="rental.CarRentalCompany.getAllCarRentalCompanies",                           
                query="SELECT comp FROM CarRentalCompany comp"),
    @NamedQuery(name="rental.CarRentalCompany.getAllCarTypesByCompany",
                query="SELECT t FROM CarRentalCompany r, IN (r.carTypes) t WHERE r.name = :company"),
    @NamedQuery(name="rental.CarRentalCompany.getCheapestCarType",
                query="SELECT ct.name FROM Car c JOIN c.type ct LEFT OUTER JOIN c.reservations res WHERE :end <= res.startDate OR res.endDate <= :start OR (res.startDate IS NULL AND res.endDate IS NULL) ORDER BY ct.rentalPricePerDay ")
    }) 


@Entity
public class CarRentalCompany {
    //static, so transient anyway
    @Transient
    private static Logger logger = Logger.getLogger(CarRentalCompany.class.getName());
    @Id
    private String name;
    @OneToMany(cascade = {CascadeType.REFRESH,CascadeType.PERSIST})
    private List<Car> cars;
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private Set<CarType> carTypes;
    
    /***************
     * CONSTRUCTOR *
     ***************/
    
    /**
     * No argument constructor, necessary for entities
     */
    public CarRentalCompany(){
        this.carTypes = new HashSet<CarType>();
    }
    
    public CarRentalCompany(String name, List<Car> cars) {
        this.carTypes = new HashSet<CarType>();
        logger.log(Level.INFO, "<{0}> Car Rental Company {0} starting up...", name);
        setName(name);
        this.cars = cars;
        for (Car car : cars) {
            carTypes.add(car.getType());
        }
    }

    /********
     * NAME *
     ********/
    
    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    /*************
     * CAR TYPES *
     *************/
    
    public Collection<CarType> getAllTypes() {
        return carTypes;
    }

    public CarType getType(String carTypeName) {
        for(CarType type:carTypes){
            if(type.getName().equals(carTypeName))
                return type;
        }
        throw new IllegalArgumentException("<" + carTypeName + "> No cartype of name " + carTypeName);
    }

    public boolean isAvailable(String carTypeName, Date start, Date end) {
        logger.log(Level.INFO, "<{0}> Checking availability for car type {1}", new Object[]{name, carTypeName});
        return getAvailableCarTypes(start, end).contains(getType(carTypeName));
    }

    public Set<CarType> getAvailableCarTypes(Date start, Date end) {
        Set<CarType> availableCarTypes = new HashSet<CarType>();
        for (Car car : cars) {
            if (car.isAvailable(start, end)) {
                availableCarTypes.add(car.getType());
            }
        }
        return availableCarTypes;
    }

    /*********
     * CARS *
     *********/
    
    public Car getCar(int uid) {
        for (Car car : cars) {
            if (car.getId() == uid) {
                return car;
            }
        }
        throw new IllegalArgumentException("<" + name + "> No car with uid " + uid);
    }

    public Set<Car> getCars(CarType type) {
        Set<Car> out = new HashSet<Car>();
        for (Car car : cars) {
            if (car.getType().equals(type)) {
                out.add(car);
            }
        }
        return out;
    }
    
     public Set<Car> getCars(String type) {
        Set<Car> out = new HashSet<Car>();
        for (Car car : cars) {
            if (type.equals(car.getType().getName())) {
                out.add(car);
            }
        }
        return out;
    }

    private List<Car> getAvailableCars(String carType, Date start, Date end) {
        List<Car> availableCars = new LinkedList<Car>();
        for (Car car : cars) {
            if (car.getType().getName().equals(carType) && car.isAvailable(start, end)) {
                availableCars.add(car);
            }
        }
        return availableCars;
    }

    /****************
     * RESERVATIONS *
     ****************/
    
    public Quote createQuote(ReservationConstraints constraints, String guest)
            throws ReservationException {
        logger.log(Level.INFO, "<{0}> Creating tentative reservation for {1} with constraints {2}",
                new Object[]{name, guest, constraints.toString()});

        CarType type = getType(constraints.getCarType());

        if (!isAvailable(constraints.getCarType(), constraints.getStartDate(), constraints.getEndDate())) {
            throw new ReservationException("<" + name
                    + "> No cars available to satisfy the given constraints.");
        }

        double price = calculateRentalPrice(type.getRentalPricePerDay(), constraints.getStartDate(), constraints.getEndDate());

        return new Quote(guest, constraints.getStartDate(), constraints.getEndDate(), getName(), constraints.getCarType(), price);
    }

    // Implementation can be subject to different pricing strategies
    private double calculateRentalPrice(double rentalPricePerDay, Date start, Date end) {
        return rentalPricePerDay * Math.ceil((end.getTime() - start.getTime())
                / (1000 * 60 * 60 * 24D));
    }

    public Reservation confirmQuote(Quote quote) throws ReservationException {
        logger.log(Level.INFO, "<{0}> Reservation of {1}", new Object[]{name, quote.toString()});
        List<Car> availableCars = getAvailableCars(quote.getCarType(), quote.getStartDate(), quote.getEndDate());
        if (availableCars.isEmpty()) {
            throw new ReservationException("Reservation failed, all cars of type " + quote.getCarType()
                    + " are unavailable from " + quote.getStartDate() + " to " + quote.getEndDate());
        }
        Car car = availableCars.get((int) (Math.random() * availableCars.size()));

        Reservation res = new Reservation(quote, car.getId());
        car.addReservation(res);
        return res;
    }

    public void cancelReservation(Reservation res) {
        logger.log(Level.INFO, "<{0}> Cancelling reservation {1}", new Object[]{name, res.toString()});
        getCar(res.getCarId()).removeReservation(res);
    }
    
    public Set<Reservation> getReservationsBy(String renter) {
        logger.log(Level.INFO, "<{0}> Retrieving reservations by {1}", new Object[]{name, renter});
        Set<Reservation> out = new HashSet<Reservation>();
        for(Car c : cars) {
            for(Reservation r : c.getReservations()) {
                if(r.getCarRenter().equals(renter))
                    out.add(r);
            }
        }
        return out;
    }

    public void addCar(Car car) {
        cars.add(car);
    }
}
