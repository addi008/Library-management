package com.library.dao;

import com.library.exception.MemberNotFoundException;
import com.library.model.Member;

import java.util.List;

public interface MemberDAO {
    boolean addMember(Member member);
    boolean updateMember(Member member);
    boolean deleteMember(int memberId);
    Member getMemberById(int memberId) throws MemberNotFoundException;
    List<Member> getAllMembers();
    Member getMemberByEmail(String email) throws MemberNotFoundException;
}
