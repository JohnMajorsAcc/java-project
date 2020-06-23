package edu.javacourse.studentorder.dao;

import edu.javacourse.studentorder.domain.CountryArea;
import edu.javacourse.studentorder.domain.PassportOffice;
import edu.javacourse.studentorder.domain.RegisterOffice;
import edu.javacourse.studentorder.domain.Street;
import edu.javacourse.studentorder.config.Config;
import edu.javacourse.studentorder.exception.DaoException;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;


//класс для обращения к справочным данным
public class DictionaryDaoImpl implements DictionaryDao{

    private static final String GET_STREET = "select street_code, street_name from jc_street where street_name like (?)";
    private static final String GET_PASSPORT = "select * from jc_passport_office " +
            "where p_office_area_id= (?)";
    private static final String GET_REGISTER = "select * from jc_register_office " +
            "where r_office_area_id= (?)";
    private static final String GET_AREA = "select * from jc_country_struct " +
            "where area_id like ? and area_id <> ?";


    //TODO refactoring - make one method!
    private Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection( //подключение к бд
                Config.getProperty(Config.DB_URL),
                Config.getProperty(Config.DB_LOGIN),
                Config.getProperty(Config.DB_PASSWORD)
                );
        return connection;
    }

    public List<Street> findStreets(String pattern) throws DaoException {

        List<Street> result = new LinkedList<>();


        try (Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(GET_STREET)) {

            statement.setString(1, "%" + pattern + "%");
            ResultSet resultSet = statement.executeQuery();//выполнили запрос

            while (resultSet.next()){ //получили ответ
                Street str = new Street(resultSet.getLong("street_code"),resultSet.getString("street_name"));
                result.add(str);
            }

        } catch (SQLException e) {
            throw new DaoException(e);
        }


        return result;
    }

    @Override
    public List<PassportOffice> findPassportOffices(String areaId) throws DaoException {
        List<PassportOffice> result = new LinkedList<>();


        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_PASSPORT)) {

            statement.setString(1, areaId);
            ResultSet resultSet = statement.executeQuery();//выполнили запрос

            while (resultSet.next()){ //получили ответ
                PassportOffice po = new PassportOffice(
                        resultSet.getLong("p_office_id"),
                        resultSet.getString("p_office_area_id"),
                        resultSet.getString("p_office_name"));
                result.add(po);
            }

        } catch (SQLException e) {
            throw new DaoException(e);
        }


        return result;
    }

    @Override
    public List<RegisterOffice> findRegistreOffices(String areaId) throws DaoException {
        List<RegisterOffice> result = new LinkedList<>();


        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_REGISTER)) {

            statement.setString(1, areaId);
            ResultSet resultSet = statement.executeQuery();//выполнили запрос

            while (resultSet.next()){ //получили ответ
                RegisterOffice ro = new RegisterOffice(
                        resultSet.getLong("r_office_id"),
                        resultSet.getString("r_office_area_id"),
                        resultSet.getString("r_office_name"));
                result.add(ro);
            }

        } catch (SQLException e) {
            throw new DaoException(e);
        }


        return result;
    }

    @Override
    public List<CountryArea> findArea(String areaId) throws DaoException {
        List<CountryArea> result = new LinkedList<>();


        try (Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(GET_AREA)) {
            String param1 = buildParam(areaId);
            String param2 = areaId;


            statement.setString(1, param1);
            statement.setString(2, param2);
            ResultSet resultSet = statement.executeQuery();//выполнили запрос

            while (resultSet.next()){ //получили ответ
                CountryArea ca = new CountryArea(
                        resultSet.getString("area_id"),
                        resultSet.getString("area_name"));
                result.add(ca);
            }

        } catch (SQLException e) {
            throw new DaoException(e);
        }


        return result;
    }

    private String buildParam(String areaId) throws SQLException {
        if (areaId == null || areaId.trim().isEmpty()){
            return "__0000000000";
        } else if (areaId.endsWith("0000000000")){
            return areaId.substring(0,2) + "___0000000";
        } else if (areaId.endsWith("0000000")){
            return areaId.substring(0,5) + "___0000";
        } else if (areaId.endsWith("0000")){
            return areaId.substring(0,8) + "____";
        }
        throw  new SQLException("Invalid parametr areaId: " + areaId);

    }
}
