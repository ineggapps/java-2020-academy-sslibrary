package dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import single.LibraryStorage;
import single.Services;
import vo.BookManageVO;
import vo.UserVO;

public class UserManageImpl implements UserManage {
	private Scanner sc = new Scanner(System.in);

	@Override
	public void join() {
		System.out.println("회원가입");
		// id(키), pw, name, email
		// id(키) 중복체크

		UserVO vo = new UserVO();
		String id;
		System.out.print("아이디 입력 >");
		id = sc.next();
		vo.setId(id);

		if (LibraryStorage.getInstance().getUser(id) != null) {
			System.out.println("등록된 아이디 입니다 ㅠㅠ. \n");
			return;
		}

		try {

			System.out.print("이름 입력 >");
			vo.setName(sc.next());

			/*
			 * System.out.print("아이디 입력 >"); vo.setId(sc.next());
			 */

			System.out.print("비밀번호 입력 >");
			vo.setPw(sc.next());

			System.out.print("이메일 입력 >");
			vo.setEmail(sc.next());

			LibraryStorage ls = LibraryStorage.getInstance();
			ls.getUserList().add(vo);// 유저 추가
			ls.sortByUserName();// 정렬 호출
			System.out.println("축> 회원가입이 완료 되었습니다. <하");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void login() {
		System.out.println("\n===로그인===");
		UserVO loginMember = LibraryStorage.getInstance().getLoginMember();
		String id;
		String pw;

		System.out.print("아이디 입력 ? ");
		id = sc.next();
		System.out.print("비밀번호 입력 ? ");
		pw = sc.next();

		if (id.equals(ADMIN_ID) && pw.equals(ADMIN_PW)) {
			System.out.println("관리자로 로그인하셨습니다!");
			LibraryStorage.getInstance().setLoginMember(new UserVO(ADMIN_ID, ADMIN_PW, "관리자"));
			return;
		}

		// 일반 유저 로그인
		UserVO vo = LibraryStorage.getInstance().getUser(id);
		if (vo == null) {
			System.out.println("존재하지 않는 계정입니다.");
			return;
		} else if (!pw.equals(vo.getPw())) {
			System.out.println("아이디와 비밀번호가 일치하지 않습니다."); // 강사님이! 아이디나 비밀번호 틀렸을 땐 알려주면 안된다고 했던거 생각해서 수정했습니다!
			return;
		}

		System.out.println(vo.getName() + "님이 로그인하셨습니다.");
		LibraryStorage.getInstance().setLoginMember(vo);
	}

	@Override
	public void logout() {
		logout(false);
	}

	public void logout(boolean isSilent) {
		LibraryStorage.getInstance().setLoginMember(null);
		if (!isSilent) {
			System.out.println("안전하게 로그아웃 되었습니다.");
		}
	}

	@Override
	public void update() {
		System.out.println("== 정보 수정...==\n");

		UserVO user = LibraryStorage.getInstance().getLoginMember();
		if (user == null) {
			System.out.println("로그인 상태가 아닙니다");
			return;
		}
		String id;
		String pw;

		System.out.println("=========본인 확인 ========");
		System.out.printf("%s(%s)님의 정보 수정\n", user.getId(), user.getName());
		id = user.getId();
		System.out.print("비밀번호 입력>");
		pw = sc.next();

		UserVO vo = LibraryStorage.getInstance().getUser(id);

		if (vo == null || !vo.getPw().equals(pw) || !vo.getId().equals(id)) {
			System.out.println("아이디와 비밀번호가 일치 하지 않아요 ㅠㅠ...\n");
			return;
		}

		System.out.print("수정할 비밀번호 >");
		vo.setPw(sc.next());

		System.out.print("수정할 이름 >");
		vo.setName(sc.next());

		System.out.print("수정할 이메일 >");
		vo.setEmail(sc.next());

		System.out.println("수정된" + vo.getName() + "님의 정보 : " + vo.toStringWithPassword());
		System.out.println(vo.getName() + "님의 정보 수정이 완료 되었습니다.! \n");
	}

	@Override
	public void out() {
		String id;
		String pw;

		System.out.print("아이디 입력 >");
		id = sc.next();

		System.out.print("비밀 번호 입력 >");
		pw = sc.next();

		UserVO vo = LibraryStorage.getInstance().getUser(id);
		if (vo == null || !vo.getPw().equals(pw) || !vo.getId().equals(id)) {
			System.out.println("아이디와 비밀번호가 일치 하지 않아요 ㅠㅠ...\n");
			return;
		}

		LibraryStorage.getInstance().getUserList().remove(vo);
		List<BookManageVO> rentalList = LibraryStorage.getInstance().getRentalList();
		List<BookManageVO> removeList = new ArrayList<>();
		// 강제 반납 대상 책 목록
		for (BookManageVO bm : rentalList) {
			if (bm.getId().equals(id)) {
				removeList.add(bm);
			}
		}
		// 반납하기
		// 반납 후 대여일지에서 해당 회원목록을 삭제
		BookTransaction bt = Services.getInstance().getBookTransaction();
		for (BookManageVO bm : removeList) {
			bt.returnBook(bm);
			rentalList.remove(bm);
		}

		logout(true);
		System.out.println(vo.getName() + " 회원님 탈퇴가 되셨습니다...\n");
	}

}
