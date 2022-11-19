import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.mybatis.example.PageCondition;
import org.mybatis.example.User;
import org.mybatis.example.UserMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author haishen
 * @date 2018/5/6
 * 二级缓存的开启需要显示指定，同时po对象需要实现序列化
 */
public class Main {
    public static void main(String[] args) throws IOException {

        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        SqlSession sqlSession = null;
        try {
            sqlSession = sqlSessionFactory.openSession();
            UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
            User user = userMapper.selectUserById(1L);
            PageCondition page = new PageCondition();
            page.setPageSize(2);
            page.setCurrentPage(1);
            List<User> list = userMapper.listByCondition(page, 1L);
            System.out.println(list.size());
            sqlSession.commit();
            System.out.println("age---->" + user.getAge());
        } catch (Exception e) {
            e.printStackTrace();
            sqlSession.rollback();
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }
        }
    }


}
