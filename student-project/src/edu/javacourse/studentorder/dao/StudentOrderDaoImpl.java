//57
package edu.javacourse.studentorder.dao;

import edu.javacourse.studentorder.config.Config;
import edu.javacourse.studentorder.domain.*;
import edu.javacourse.studentorder.exception.DaoException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

public class StudentOrderDaoImpl implements StudentOrderDao {
    private static final String INSERT_ORDER =
            "INSERT INTO jc_student_order(" +
                    "            student_order_status, student_order_date, h_sur_name, " +
                    "            h_given_name, h_patronymic, h_date_of_birth, h_passport_seria, " +
                    "            h_passport_number, h_passport_date, h_passport_office_id, h_post_index, " +
                    "            h_street_code, h_building, h_extension, h_apartment, h_university_id, h_student_number," +
                    "            w_sur_name, w_given_name, w_patronymic, w_date_of_birth, w_passport_seria, " +
                    "            w_passport_number, w_passport_date, w_passport_office_id, w_post_index, " +
                    "           w_street_code, w_building, w_extension, w_apartment, w_university_id, w_student_number," +
                    "            certificate_id, register_office_id, marriage_date)" +
                    "    VALUES ( ?, ?, ?, " +
                    "            ?, ?, ?, ?, " +
                    "            ?, ?, ?, ?, " +
                    "            ?, ?, ?, ?, ?, ?,  " +
                    "            ?, ?, ?, ?, ?, " +
                    "            ?, ?, ?, ?, " +
                    "            ?, ?, ?, ?, ?, ?, " +
                    "            ?, ?, ?);";

    private static final String INSERT_CHILD =
            "INSERT INTO public.jc_student_child(" +
                    "            student_order_id, c_sur_name, c_given_name, " +
                    "            c_patronymic, c_date_of_birth, c_certificate_number, c_certificate_date, " +
                    "            c_register_office_id, c_post_index, c_street_code, c_building, " +
                    "            c_extension, c_apartment)" +
                    "    VALUES (?, ?, ?, " +
                    "            ?, ?, ?, ?, " +
                    "            ?, ?, ?, ?, " +
                    "            ?, ?);";

    private static final String SELECT_ORDERS =

            "select so.*, ro.r_office_area_id, ro.r_office_name from jc_student_order so " +
                    "inner join jc_register_office ro on ro.r_office_id = so.register_office_id " +
                    "where student_order_status = 0 order by student_order_date";


    //TODO refactoring - make one method!
    private Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection( //подключение к бд
                Config.getProperty(Config.DB_URL),
                Config.getProperty(Config.DB_LOGIN),
                Config.getProperty(Config.DB_PASSWORD)
        );
        return connection;
    }

    @Override
    public Long saveStudentOrder(StudentOrder so) throws DaoException {
        Long result = -1L;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_ORDER, new String[]{"student_order_id"})) {

            connection.setAutoCommit(false); //означает что мы сами управляем транзакцией

            try {
                //header
                statement.setInt(1, StudentOrderStatus.START.ordinal());
                statement.setTimestamp(2, java.sql.Timestamp.valueOf(LocalDateTime.now()));

                //husband data
                setParamsForAdult(statement, 3, so.getHusband());

                //wife data
                setParamsForAdult(statement, 18, so.getWife());

                //marriage
                statement.setString(33, so.getMarriageCertificateId());
                statement.setLong(34, so.getMarriageOffice().getOfficeId());
                statement.setDate(35, java.sql.Date.valueOf(so.getMarriageDate()));

                statement.executeUpdate();

                ResultSet generatedKeys = statement.getGeneratedKeys();// метод возвращает все сгенерированные поля
                if (generatedKeys.next()) {
                    result = generatedKeys.getLong(1);
                }
                generatedKeys.close();

                saveChildren(connection, so, result);

                connection.commit();    //подтверждение транзакции

            } catch (SQLException sqlException) {
                connection.rollback(); // отмена транзакции
                throw sqlException;
            }


        } catch (SQLException e) {
            throw new DaoException(e);
        }

        return result;
    }


    private void saveChildren(Connection connection, StudentOrder so, Long soId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_CHILD)) {
            for (Child child : so.getChildren()) {
                statement.setLong(1, soId);
                setParamsForChild(statement, child);
                statement.addBatch(); //накапливает количество изменений и потом производит их через экзекьют
            }
            statement.executeBatch();
        }
    }

    private void setParamsForAdult(PreparedStatement statement, int start, Adult adult) throws SQLException {
        setParamsForPerson(statement, start, adult);

        statement.setString(start + 4, adult.getPassportSeria());
        statement.setString(start + 5, adult.getPassportNumber());
        statement.setDate(start + 6, Date.valueOf(adult.getIssueDate()));
        statement.setLong(start + 7, adult.getIssueDepartment().getOfficeId());
        setParamsForAddress(statement, start + 8, adult);
        statement.setLong(start + 13, adult.getUniversity().getUniversityId());
        statement.setString(start + 14, adult.getStudentId());
    }

    private void setParamsForChild(PreparedStatement statement, Child child) throws SQLException {
        setParamsForPerson(statement, 2, child);

        statement.setString(6, child.getCertificateNumber());
        statement.setDate(7, java.sql.Date.valueOf(child.getIssueDate()));
        statement.setLong(8, child.getIssueDepartment().getOfficeId());
        setParamsForAddress(statement, 9, child);
    }

    private void setParamsForPerson(PreparedStatement statement, int start, Person person) throws SQLException {
        statement.setString(start, person.getSurName());
        statement.setString(start + 1, person.getGivenName());
        statement.setString(start + 2, person.getPatronymic());
        statement.setDate(start + 3, Date.valueOf(person.getDateOfBirth()));
    }

    private void setParamsForAddress(PreparedStatement statement, int start, Person person) throws SQLException {
        Address address = person.getAddress();
        statement.setString(start, address.getPostCode());
        statement.setLong(start + 1, address.getStreet().getStreetCode());
        statement.setString(start + 2, address.getBuilding());
        statement.setString(start + 3, address.getExtension());
        statement.setString(start + 4, address.getApartment());
    }

    @Override
    public List<StudentOrder> getStudentOrders() throws DaoException {
        List<StudentOrder> result = new LinkedList<>();
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SELECT_ORDERS);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                StudentOrder studentOrder = new StudentOrder();

                fillStudentOrder(resultSet, studentOrder);
                fillMarriage(resultSet, studentOrder);

                Adult husband = fillAdult(resultSet, "h_");
                Adult wife = fillAdult(resultSet, "w_");

                studentOrder.setHusband(husband);
                studentOrder.setWife(wife);

                result.add(studentOrder);
            }

        } catch (SQLException sqlException) {
            throw new DaoException(sqlException);
        }

        return result;
    }

    private Adult fillAdult(ResultSet resultSet, String prefix) throws SQLException {
        Adult adult = new Adult();
        adult.setSurName(resultSet.getString(prefix + "sur_name"));
        adult.setGivenName(resultSet.getString(prefix + "given_name"));
        adult.setPatronymic(resultSet.getString(prefix + "patronymic"));
        adult.setDateOfBirth(resultSet.getDate(prefix +"date_of_birth").toLocalDate());
        adult.setPassportSeria(resultSet.getString(prefix +"passport_seria"));
        adult.setPassportNumber(resultSet.getString(prefix +"passport_number"));
        adult.setIssueDate(resultSet.getDate(prefix + "passport_date").toLocalDate());

        PassportOffice passportOffice = new PassportOffice(resultSet.getLong(prefix + "passport_office_id"), "", "");
        adult.setIssueDepartment(passportOffice);

        Address address = new Address();
        address.setPostCode(resultSet.getString(prefix + "post_index"));
        address.setBuilding(resultSet.getString(prefix + "building"));
        address.setExtension(resultSet.getString(prefix + "extension"));
        address.setApartment(resultSet.getString(prefix+ "apartment"));

        Street street = new Street(resultSet.getLong(prefix + "street_code"), "");
        address.setStreet(street);

        adult.setAddress(address);

        University university = new University(resultSet.getLong(prefix + "university_id"), "");
        adult.setUniversity(university);

        adult.setStudentId(resultSet.getString(prefix + "student_number"));
        return adult;

    }

    private void fillStudentOrder(ResultSet resultSet, StudentOrder studentOrder) throws SQLException {
        studentOrder.setStudentOrderId(resultSet.getLong("student_order_id"));
        studentOrder.setStudentOrderDate(resultSet.getTimestamp("student_order_date").toLocalDateTime());   // тут дата и время заявки, поэтому Timestamp&LocalDateTime
        studentOrder.setStudentOrderStatus(StudentOrderStatus.fromValue(resultSet.getInt("student_order_status")));
    }

    private void fillMarriage(ResultSet resultSet, StudentOrder studentOrder) throws SQLException {
        studentOrder.setMarriageCertificateId(resultSet.getString("certificate_id"));
        studentOrder.setMarriageDate(resultSet.getDate("marriage_date").toLocalDate()); //тут дата, без времени, поэтому localDate

        Long registerOfficeId = resultSet.getLong("register_office_id");
        String officeAreaId = resultSet.getString("r_office_area_id");
        String officeName = resultSet.getString("r_office_name");

        RegisterOffice registerOffice = new RegisterOffice(registerOfficeId, officeAreaId, officeName);


        studentOrder.setMarriageOffice(registerOffice);
    }

}
