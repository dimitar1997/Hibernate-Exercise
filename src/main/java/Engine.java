import entities.Address;
import entities.Employee;
import entities.Project;
import entities.Town;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class Engine implements Runnable {
    private final EntityManager entityManager;
    private final BufferedReader bufferedReader;

    public Engine(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    public void run() {
        // first task
        // removeObjects();

        // try {
        // containsEmployee();
        // } catch (IOException e) {
        //   e.printStackTrace();
        //  }

        //  salaryAbove50000();

        //  employeesFromDepartment();

//        try {
//            newAddressUpdatingEmployee();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        // addressesEmployeeCount();

//        try {
//            getEmployeeProject();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        // findLatest10Projects();

        // increaseSalaries();

        try {
            deleteAddresses();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void deleteAddresses() throws IOException {
        System.out.println("Enter Town:");
        String townName = bufferedReader.readLine();

        Town town = this.entityManager.createQuery("select t from Town as t " +
                "where t.name = :name", Town.class).setParameter("name", townName).getSingleResult();

        this.entityManager.getTransaction().begin();

        List<Address> addresses = this.entityManager.createQuery("select a from Address as a " +
                "where a.town.id = :id1", Address.class)
                .setParameter("id1", town.getId()).getResultList();


        this.entityManager.getTransaction().begin();
        for (Address address : addresses) {
            this.entityManager.remove(address);
        }
        this.entityManager.remove(town);

        this.entityManager.getTransaction().commit();

        System.out.printf("%d address in %s deleted", addresses.size(), town.getName());
    }


    private void increaseSalaries() {
        this.entityManager.getTransaction().begin();
        this.entityManager.createQuery("UPDATE Employee as e " +
                "set e.salary = e.salary * 1.2 " +
                "where e.department.id in :ds").setParameter("ds", Set.of(1, 2, 4, 11)).executeUpdate();

        entityManager.getTransaction().commit();
        List<Employee> employees = this.entityManager.createQuery("select e from Employee as e " +
                "where e.department.id in :ds", Employee.class).setParameter("ds", Set.of(1, 2, 4, 11)).getResultList();

        for (Employee employee : employees) {
            System.out.printf("%s %s ($%.2f)%n", employee.getFirstName(), employee.getLastName(),
                    employee.getSalary());
        }
    }

    private void findLatest10Projects() {
        List<Project> projects = this.entityManager.createQuery("select p from Project as p " +
                "order by p.startDate desc").setMaxResults(10).getResultList();

        projects.stream().sorted(Comparator.comparing(Project::getName))
                .forEach(project ->
                        System.out.printf("Project name: %s%n" +
                                        "Project Description: %s\n" +
                                        " \tProject Start Date: %s\n" +
                                        " \tProject End Date: %s\n", project.getName(), project.getDescription(),
                                project.getStartDate(), project.getEndDate()));
    }

    private void getEmployeeProject() throws IOException {
        System.out.println("Enter employee id:");
        int employee = Integer.parseInt(bufferedReader.readLine());

        Employee employee1 = this.entityManager.find(Employee.class, employee);


        System.out.printf("%s  %s - %s%n", employee1.getFirstName(), employee1.getLastName(),
                employee1.getJobTitle());
        employee1.getProjects().stream().sorted(Comparator.comparing(Project::getName)).forEach(project ->
                System.out.println(project.getName()));

    }

    private void addressesEmployeeCount() {
        List<Address> addresses = this.entityManager.createQuery("select  a from Address as a " +
                "order by a.employees.size desc", Address.class).setMaxResults(10).getResultList();

        addresses.forEach(address ->
                System.out.printf("%s %s - %d employees%n", address.getText(),
                        address.getTown() == null ? "Unknown" : address.getTown().getName(),
                        address.getEmployees().size()));
    }

    private void newAddressUpdatingEmployee() throws IOException {
        System.out.println("Enter last name:");
        String lastName = bufferedReader.readLine();

        Employee employee = this.entityManager.createQuery("select e from Employee as e " +
                "where e.lastName = :name", Employee.class)
                .setParameter("name", lastName).getSingleResult();
        Address address = new Address();
        address.setText("Vitoshka 15");

        this.entityManager.getTransaction().begin();
        this.entityManager.persist(address);
        this.entityManager.getTransaction().commit();

        this.entityManager.getTransaction().begin();
        employee.setAddress(address);
        this.entityManager.getTransaction().commit();

    }

    private void employeesFromDepartment() {

        List<Employee> employees = this.entityManager.createQuery("select e from Employee  as e " +
                "where e.department.id = 6 " +
                "order by e.salary, e.id").getResultList();

        for (Employee employee : employees) {
            System.out.printf("%s %s from %s - $%.2f\n", employee.getFirstName(),
                    employee.getLastName(), employee.getDepartment().getName(), employee.getSalary());
        }
    }

    private void salaryAbove50000() {
        List<Employee> employees = this.entityManager.createQuery("select e from Employee as e " +
                "where e.salary > 50000").getResultList();
        for (Employee employee : employees) {
            System.out.println(employee.getFirstName());
        }
    }


    private void containsEmployee() throws IOException {
        System.out.println("Enter name:");
        String name = bufferedReader.readLine();
        try {
            Employee employee = this.entityManager.createQuery(
                    "select e from Employee as e " +
                            "where concat(e.firstName,' ', e.lastName) = :name ", Employee.class)
                    .setParameter("name", name).getSingleResult();
            System.out.println("Yes");
        } catch (NoResultException exception) {
            System.out.println("No");
        }

    }

    private void removeObjects() {
        List<Town> towns = this.entityManager.createQuery("SELECT t  from Town AS t " +
                "where length(t.name) > 5", Town.class)
                .getResultList();
        this.entityManager.getTransaction().begin();
        for (Town town : towns) {
            entityManager.merge(town);
        }

        for (Town town : towns) {
            town.setName(town.getName().toLowerCase(Locale.ROOT));
        }
        this.entityManager.getTransaction().commit();
    }
}
