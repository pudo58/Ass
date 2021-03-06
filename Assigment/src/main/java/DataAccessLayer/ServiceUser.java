package DataAccessLayer;

import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import BussinessLayer.ServiceInterface.IServiceUser;
import BussinessLayer.Utils.JpaUtil;
import BussinessLayer.Utils.SecurityUtil;
import BussinessLayer.entities.User;

public class ServiceUser implements IServiceUser{
	public ServiceUser() {
		em=JpaUtil.createEntityManager();
	}
	EntityManager em;
	
	@Override
	public User add(User entity) throws Exception {
		try {
			em.getTransaction().begin();
			em.persist(entity);
			em.flush();
			em.getTransaction().commit();
			return entity;
		} catch (Exception e) {
			e.printStackTrace();
			em.getTransaction().rollback();
			throw e;
		}
	}

	@Override
	public User delete(User entity) throws Exception {
		try {
			em.getTransaction().begin();
			em.remove(entity);
			em.flush();
			em.getTransaction().commit();
			return entity;
		} catch (Exception e) {
			e.printStackTrace();
			em.getTransaction().rollback();
			throw e;
		}
	}

	@Override
	public User update(User entity) throws Exception {
		try {
			em.getTransaction().begin();
			em.merge(entity);
			em.flush();
			em.getTransaction().commit();
			return entity;
		} catch (Exception e) {
			e.printStackTrace();
			em.getTransaction().rollback();
			throw e;
		}
	}

	@Override
	public User findById(int id) throws Exception {
		return em.find(User.class,id);
	}

	@Override
	public List<User> selectAll() throws Exception {
	String query="SELECT u FROM User u";
	TypedQuery<User> list=em.createQuery(query, User.class);
	return list.getResultList();
	}

	@Override
	public User findByUsername(String username) throws Exception {
		String query="SELECT u FROM User u WHERE u.username=:username";
		User u=em.createQuery(query,User.class).setParameter("username", username).getSingleResult();
		return u;
	}

	@Override
	public User login(String username,String password) throws Exception {
		String query="SELECT u FROM User u WHERE u.username=:username and u.password=:password";
		User u=em.createQuery(query,User.class).setParameter("username", username).
				setParameter("password", password).getSingleResult();
		return u;
	}

	@Override
	public HashMap<String, String> listError(User entity,String passwd) throws Exception {
		HashMap<String, String> list_error=new HashMap<>();
		if(entity.getUsername().isBlank()) {
			list_error.put("username_space", "T??i kho???n kh??ng ch???a kho???ng tr???ng ho???c r???ng");
		}
		else if(entity.getUsername().length()<5) {
			list_error.put("username_length", "T??i kho???n ph???i tr??n 5 k?? t???");
		}
		else if(!entity.getUsername().matches("^[a-zA-Z0-9]+$")) {
			list_error.put("username_invalid", "T??i kho???n kh??ng ch???a  k?? t??? ?????c bi???t");
			
		}else if(SecurityUtil.isXSS(entity.getUsername())) {
			list_error.put("username_xss", "T??i kho???n kh??ng ????ng ?????nh d???ng");
		}
		else if(isExistUsername(entity.getUsername())) {
			list_error.put("username_exist", "T??n ????ng nh???p ???? t???n t???i ");
		}else if(entity.getUsername().length()>20) {
			list_error.put("username_overflow", "T??n ????ng nh???p kh??ng qu?? 20 k?? t???");
		}
		if(passwd.isBlank()) {
			list_error.put("password_space", "M???t kh???u kh??ng ch???a kho???ng tr???ng ho???c r???ng");
		}
		else if(passwd.trim().length()<5) {
			list_error.put("password_length", "M???t kh???u ph???i tr??n 5 k?? t???");
		}
		else if(SecurityUtil.isXSS(passwd)) {
			list_error.put("password_xss", "M???t kh???u kh??ng ch???a k?? t??? ?????c bi???t");
		}
		else if(!passwd.trim().matches("^[a-zA-Z0-9]+$")) {
			list_error.put("password_invalid", "M???t kh???u kh??ng ch???a  k?? t??? ?????c bi???t");
			
		}
		if(entity.getDiachi().contains("<")) {
			list_error.put("diachi_invalid", "?????a ch??? kh??ng ch???a  k?? t??? ?????c bi???t");
		}
		if(entity.getEmail().isBlank()) {
			list_error.put("email_space", "Email kh??ng ???????c ????? tr???ng ho???c ch???a kho???ng tr???ng");
		}else if(entity.getEmail().trim().length()<7) {
			list_error.put("email_length", "Email ph???i tr??n 7 k?? t???");
		}else if(!entity.getEmail().trim().contains("@")) {
			list_error.put("email_invalid", "Email ch??a ????ng ?????nh d???ng");		
		}else if(SecurityUtil.checkMailClone(entity.getEmail().trim(), selectAll())) {
			list_error.put("email_clone", "Email ch??a ???????c b???o m???t");	
		}
		return list_error;
	}
	private boolean isExistUsername(String username) throws Exception {
		for(User x:selectAll()) {
			if(username.equals(x.getUsername()))
				return true;			
		}
		return false;
	}
	private boolean checkLogin(String username,String password) throws Exception {
		for(User x:selectAll()) {
			if(username.equals(x.getUsername())&&password.equals(x.getPassword()))
				return true;			
		}
		return false;
	}
	@Override
	public HashMap<String, String> checkError(String username,String passwd) throws Exception {
		HashMap<String, String>flagError=new HashMap<String, String>();
		if(username.isBlank()) {
			flagError.put("username_null", "T??i kho???n kh??ng ???????c ????? tr???ng");
		}
		else if(username.length()<5) {
			flagError.put("username_length", "T??i kho???ng ph???i l???n h??n 6 k?? t???");
		}
		else if(!isExistUsername(username)) {
			flagError.put("username_exist", "T??i kho???n  kh??ng t???n t???i");
		}
		 if(passwd.isBlank()) {
			flagError.put("passwd_null", "M???t kh???u kh??ng ???????c ????? tr???ng");
		}
		 else if(passwd.length()<5) {
			flagError.put("passwd_length", "m???t kh???u ph???i l???n h??n 6 k?? t???");
		}
		else if(!checkLogin(username, passwd)) {
			flagError.put("account_exist", "T??i kho???n ho???c m???t kh???u kh??ng ch??nh x??c");
		}
		
		return flagError;
		
	}

}
