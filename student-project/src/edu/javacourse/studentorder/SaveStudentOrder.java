package edu.javacourse.studentorder;


import edu.javacourse.studentorder.dao.StudentOrderDaoImpl;
import edu.javacourse.studentorder.dao.StudentOrderDao;
import edu.javacourse.studentorder.domain.*;
import edu.javacourse.studentorder.exception.DaoException;

import java.time.LocalDate;
import java.util.List;

public class SaveStudentOrder {
    public static void main(String[] args) throws DaoException {

//        List<Street> streets = new DictionaryDaoImpl().findStreets("про");
//        for(Street street : streets){
//            System.out.println(street.getStreetName());
//        }
//
//        List<PassportOffice> po = new DictionaryDaoImpl().findPassportOffices("010020000000");
//        for (PassportOffice p : po){
//            System.out.println(p.getOfficeName());
//        }
//
//        List<RegisterOffice> ro = new DictionaryDaoImpl().findRegistreOffices("010010000000");
//        for (RegisterOffice r : ro){
//            System.out.println(r.getOfficeName());
//        }
//
//        List<CountryArea> ca1 = new DictionaryDaoImpl().findArea("");
//        for (CountryArea c : ca1){
//            System.out.println(c.getAreaId() + " : " + c.getAreaName());
//        }
//
//        List<CountryArea> ca2= new DictionaryDaoImpl().findArea("020000000000");
//        for (CountryArea c : ca2){
//            System.out.println(c.getAreaId() + " : " + c.getAreaName());
//        }
//
//        List<CountryArea> ca3= new DictionaryDaoImpl().findArea("020020000000");
//        for (CountryArea c : ca3){
//            System.out.println(c.getAreaId() + " : " + c.getAreaName());
//        }
//
//        List<CountryArea> ca4= new DictionaryDaoImpl().findArea("020020010000");
//        for (CountryArea c : ca4){
//            System.out.println(c.getAreaId() + " : " + c.getAreaName());
//        }



//        StudentOrder studentOrder = buildStudentOrder(10);
        StudentOrderDao studentOrderDao = new StudentOrderDaoImpl();
//        Long studentOrderId = studentOrderDao.saveStudentOrder(studentOrder);
//        System.out.println(studentOrderId);

        List<StudentOrder> listOrders = studentOrderDao.getStudentOrders();
        listOrders.stream()
                .forEach((p) -> System.out.println((p.getHusband() + " "  + p.getHusband().getAddress() + " " + p.getWife() + " " + p.getWife().getAddress())));


//        StudentOrder so = new StudentOrder();
//        long ans = saveStudentOrder(so);
//        System.out.println(ans);
    }

    static long saveStudentOrder(StudentOrder studentOrder) {
        long answer = 199;
        System.out.println("saveStudentOrder");

        return answer;
    }

    public static StudentOrder buildStudentOrder(long id) {
        StudentOrder so = new StudentOrder();
        so.setStudentOrderId(id);
        so.setMarriageCertificateId("" + (123456000 + id));
        so.setMarriageDate(LocalDate.of(2016, 7, 4));

        RegisterOffice ro = new RegisterOffice(1L, "", "");
        so.setMarriageOffice(ro);

        Street street = new Street(1L, "Street first");

        Address address = new Address("195000", street, "12", "", "142");

        // Муж
        Adult husband = new Adult("Петров", "Виктор", "Сергеевич", LocalDate.of(1997, 8, 24));
        husband.setPassportSeria("" + (1000 + id));
        husband.setPassportNumber("" + (100000 + id));
        husband.setIssueDate(LocalDate.of(2017, 9, 15));
        PassportOffice poHusb = new PassportOffice(1L, "", "");
        husband.setIssueDepartment(poHusb);
        husband.setStudentId("" + (100000 + id));
        husband.setAddress(address);
        husband.setUniversity(new University(2L, "U1"));
        husband.setStudentId("HH12345");
        // Жена
        Adult wife = new Adult("Петрова", "Вероника", "Алекссевна", LocalDate.of(1998, 3, 12));
        wife.setPassportSeria("" + (2000 + id));
        wife.setPassportNumber("" + (200000 + id));
        wife.setIssueDate(LocalDate.of(2010, 12, 15));
        PassportOffice poWife = new PassportOffice(2L, "", "");
        wife.setIssueDepartment(poWife);
        wife.setStudentId("" + (200000 + id));
        wife.setAddress(address);
        wife.setUniversity(new University(3L, "U2"));
        wife.setStudentId("WW54321");

        // Ребенок
        Child child1 = new Child("Петрова", "Ирина", "Викторовна", LocalDate.of(2018, 6, 29));
        child1.setCertificateNumber("" + (300000 + id));
        child1.setIssueDate(LocalDate.of(2018, 7, 19));
        RegisterOffice roCh1 = new RegisterOffice(1L, "", "");
        child1.setIssueDepartment(roCh1);
        child1.setAddress(address);
        // Ребенок
        Child child2 = new Child("Петров", "Евгений", "Викторович", LocalDate.of(2018, 6, 29));
        child2.setCertificateNumber("" + (400000 + id));
        child2.setIssueDate(LocalDate.of(2018, 2, 11));
        RegisterOffice roCh2 = new RegisterOffice(2L, "", "");
        child2.setIssueDepartment(roCh2);
        child2.setAddress(address);

        so.setHusband(husband);
        so.setWife(wife);
        so.addChild(child1);
        so.addChild(child2);

        return so;
    }
}

