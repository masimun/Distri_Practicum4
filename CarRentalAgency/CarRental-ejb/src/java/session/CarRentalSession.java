package session;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;
import javax.transaction.UserTransaction;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Quote;
import rental.Reservation;
import rental.ReservationConstraints;
import rental.ReservationException;

@Stateful
public class CarRentalSession implements CarRentalSessionRemote {

    @PersistenceContext
    private EntityManager em;
    
    @Resource
    private SessionContext context;
    
    private String renter;
    private List<Quote> quotes = new LinkedList<Quote>();

    @Override
    public Set<String> getAllRentalCompanies() {
        Set<String> companies = new HashSet(
                    (List<String>) em.createNamedQuery(
                    "rental.CarRentalCompany.getAllCarRentalCompanyNames")
                    .getResultList());
        
            return companies;
        
    }
    
    @Override
    public List<CarType> getAvailableCarTypes(Date start, Date end) {
        
        List<CarType> availableCarTypes = new LinkedList<CarType>();
        for(CarRentalCompany crc : (List<CarRentalCompany>) 
                em.createNamedQuery("rental.CarRentalCompany.getAllCarRentalCompanies",
                        CarRentalCompany.class).getResultList()) {
            
            for(CarType ct : crc.getAvailableCarTypes(start, end)) {
                if(!availableCarTypes.contains(ct))
                    availableCarTypes.add(ct);
            }
        }
        return availableCarTypes;

    }

    @Override
    public Quote createQuote(String companyName, ReservationConstraints constraints) throws ReservationException {
        try {
            CarRentalCompany company = em.find(CarRentalCompany.class, companyName.toLowerCase());
            Quote out = company.createQuote(constraints, renter);
            quotes.add(out);
            System.out.println(out.toString());
            return out;
        } catch(Exception e) {
            throw new ReservationException(e);
        }
        
    }

    @Override
    public List<Quote> getCurrentQuotes() {
        return quotes;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public List<Reservation> confirmQuotes() throws ReservationException {
        List<Reservation> done = new LinkedList<Reservation>();
        CarRentalCompany company;
        try {
            for (Quote quote : quotes) {
                company = (CarRentalCompany) em.find(CarRentalCompany.class, quote.getRentalCompany());
                done.add(company.confirmQuote(quote));
            }
        } catch (Exception e) {
            context.setRollbackOnly();
            throw new ReservationException(e);
        }
        return done;
    }

    @Override
    public void setRenterName(String name) {
        if (renter != null) {
            throw new IllegalStateException("name already set");
        }
        renter = name;
    }

    @Override
    public String getCheapestCarType(Date start, Date end) {
        return (String) em.createNamedQuery("rental.CarRentalCompany.getCheapestCarType")
                .setParameter("start",start, TemporalType.DATE)
                .setParameter("end",end, TemporalType.DATE)
                .getResultList().get(0);

    }
}
