package rental;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class Car {
    
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private int id;
    
    @OneToOne(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    private CarType type;
    
    @OneToMany(cascade = {CascadeType.MERGE,CascadeType.REFRESH,CascadeType.PERSIST})
    private Set<Reservation> reservations;

    /***************
     * CONSTRUCTOR *
     ***************/
    
    /**
     * No argument constructor, necessary for entities
     */
    public Car(){
        
    }
    
    public Car(CarType type){
        this.type = type;
        this.reservations = new HashSet<Reservation>();
    }
    
    public Car(int uid, CarType type) {
    	this.id = uid;
        this.type = type;
        this.reservations = new HashSet<Reservation>();
    }

    /******
     * ID *
     ******/
    
    public int getId() {
    	return id;
    }
    
    /************
     * CAR TYPE *
     ************/
    
    public CarType getType() {
        return type;
    }

    /****************
     * RESERVATIONS *
     ****************/

    public boolean isAvailable(Date start, Date end) {
        if(!start.before(end))
            throw new IllegalArgumentException("Illegal given period");

        for(Reservation reservation : reservations) {
            if(reservation.getEndDate().before(start) || reservation.getStartDate().after(end))
                continue;
            return false;
        }
        return true;
    }
    
    public void addReservation(Reservation res) {
        reservations.add(res);
    }
    
    public void removeReservation(Reservation reservation) {
        // equals-method for Reservation is required!
        reservations.remove(reservation);
    }

    public Set<Reservation> getReservations() {
        return reservations;
    }
}