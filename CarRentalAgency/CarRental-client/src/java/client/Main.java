package client;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.naming.InitialContext;
import rental.CarType;
import rental.Reservation;
import rental.ReservationConstraints;
import session.CarRentalSessionRemote;
import session.ManagerSessionRemote;

public class Main extends AbstractTestManagement<CarRentalSessionRemote, ManagerSessionRemote> {

    public Main(String scriptFile) {
        super(scriptFile);
    }

    public static void main(String[] args) throws Exception {
        //TODO: use updated manager interface to load cars into companies
        Main m = new Main("trips");
        
        ManagerSessionRemote man = m.getNewManagerSession("jolo", "dockx");
        
        
        m.loadData(man);
        
        //System.out.println(man.getCarIds("hertz", "MPV"));
        //System.out.println(man.getCarIds("hertz", "Premium"));
        //System.out.println(man.getCarIds("dockx", "Special"));
        
        m.run();
        /*CarRentalSessionRemote res = m.getNewReservationSession("max");
        System.out.println(res.getAllRentalCompanies());
        System.out.println(man.getCarTypes("dockx"));
        
        System.out.println
       
        
        //m.loadData(man);*/
        
    }
    
    @Override
    protected CarRentalSessionRemote getNewReservationSession(String name) throws Exception {
        CarRentalSessionRemote out = (CarRentalSessionRemote) new InitialContext().lookup(CarRentalSessionRemote.class.getName());
        out.setRenterName(name);
        return out;
    }

    @Override
    protected ManagerSessionRemote getNewManagerSession(String name, String carRentalName) throws Exception {
        ManagerSessionRemote out = (ManagerSessionRemote) new InitialContext().lookup(ManagerSessionRemote.class.getName());
        return out;
    }
    
    @Override
    protected void checkForAvailableCarTypes(CarRentalSessionRemote session, Date start, Date end) throws Exception {
        System.out.println("Available car types between "+start+" and "+end+":");
        for(CarType ct : session.getAvailableCarTypes(start, end))
            System.out.println("\t"+ct.toString());
        System.out.println();
    }

    @Override
    protected void addQuoteToSession(CarRentalSessionRemote session, String name, Date start, Date end, String carType, String carRentalName) throws Exception {
        session.createQuote(carRentalName, new ReservationConstraints(start, end, carType));
    }

    
    @Override
    protected int getNumberOfReservationsBy(ManagerSessionRemote ms, String renterName) throws Exception {
        return ms.getNumberOfReservationsDoneBy(renterName);
    }

    @Override
    protected int getNumberOfReservationsForCarType(ManagerSessionRemote ms, String company, String carType) throws Exception {
        return ms.getNumberOfReservations(company, carType);
    }

    
    protected CarType getMostPopularCarTypeIn(ManagerSessionRemote ms, String carRentalCompanyName) throws Exception {
        return ms.getMostPopularCarType(carRentalCompanyName);
    }

   
    protected Set<String> getBestClients(ManagerSessionRemote ms) throws Exception {
        return new HashSet<String>(ms.getBestClients());
    }

    @Override
    protected String getCheapestCarType(CarRentalSessionRemote session, Date start, Date end) throws Exception {
        return session.getCheapestCarType(start, end);
    }


    private void loadData(ManagerSessionRemote man) {
        man.addCarRentalComapany("hertz");
        man.addCarRentalComapany("dockx");
    }
    
    protected void createCarType(ManagerSessionRemote ms, String name, int nbOfSeats, 
            float trunkSpace, double rentalPricePerDay, boolean smokingAllowed) {
        ms.addCarType(name, nbOfSeats, trunkSpace, rentalPricePerDay, smokingAllowed);
    }
    
    protected void createCar(ManagerSessionRemote ms, String company,int amount, CarType type) {
        ms.addCar(company, amount, type);
    }

    @Override
    protected String getMostPopularCarRentalCompany(ManagerSessionRemote ms) throws Exception {
        return ms.getMostPopularCarRentalCompany();
    }

    @Override
    protected List<Reservation> confirmQuotes(CarRentalSessionRemote session, String name) throws Exception {
        return session.confirmQuotes();
    }

}
