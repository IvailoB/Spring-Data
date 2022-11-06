import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Main {
    public static void main(String[] args) {
        //Just run and follow steps for every single exercise! TNX :)

        EntityManagerFactory emf = Persistence
                .createEntityManagerFactory("soft_uni");

        EntityManager em = emf.createEntityManager();

        Engine engine = new Engine(em);

        engine.run();


    }
}
