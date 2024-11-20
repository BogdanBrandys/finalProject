package com.kodilla.finalProject.repository;

import com.kodilla.finalProject.event.UserAction;
import org.springframework.data.repository.CrudRepository;

public interface UserActionRepository extends CrudRepository<UserAction, Long> {

}
