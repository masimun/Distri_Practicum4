package session;

import java.util.List;
import java.util.Set;
import javax.ejb.Remote;
import rental.CarType;

@Remote
public interface ManagerSessionRemote {
    
    public Set<String> getAllRentalCompanies();
    
    public List<CarType> getCarTypes(String company);
    
    public int getNumberOfReservations(String company, String type);
    
    public int getNumberOfReservationsDoneBy(String renter);
    
    public List<String> getBestClients();
    
    public CarType getMostPopularCarType(String company);
    
    public Set<Integer> getCarIds(String company,String type);
    
    public void addCarType(String name, int nbOfSeats, float trunkSpace, double rentalPricePerDay, boolean smokingAllowed);
    
    public void addCar(String Company, int amount, CarType ct);
    
    public void addCarRentalComapany(String compName);
    
    public String getMostPopularCarRentalCompany();
    
}