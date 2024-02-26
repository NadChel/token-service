package com.example.tokenservice.testUtil;

import com.example.tokenservice.data.entity.User;
import lombok.SneakyThrows;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class UserUtil {
    private UserUtil() {
    }

    public static User cloneAndMutate(User user, Consumer<User> mutator) {
        User userCopy = clone(user);
        mutator.accept(userCopy);
        return userCopy;
    }

    public static User clone(User user) {
        User userCopy = new User();
        userCopy.setUsername(user.getUsername());
        userCopy.setPassword(user.getPassword());
        userCopy.setId(user.getId());
        userCopy.setEnabled(user.getEnabled());
        userCopy.setAuthorities(user.getAuthorities()
                .stream()
                .collect(Collectors.toCollection(ConcurrentHashMap::newKeySet)));
        return userCopy;
    }

    @SneakyThrows
    public static <T extends UserDetails> boolean haveEqualFields(T oneUser, T anotherUser) {
        Field[] oneUserFields = oneUser.getClass().getDeclaredFields();
        Field[] anotherUserFields = anotherUser.getClass().getDeclaredFields();
        for (int i = 0; i < oneUserFields.length; i++) {
            Field oneUserField = oneUserFields[i];
            oneUserField.setAccessible(true);

            Field anotherUserField = anotherUserFields[i];
            anotherUserField.setAccessible(true);

            boolean areFieldsEqual = Objects.equals(oneUserField.get(oneUser), anotherUserField.get(anotherUser));

            if (!areFieldsEqual) return false;
        }
        return true;
    }
}
