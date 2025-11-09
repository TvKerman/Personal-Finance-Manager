package com.finance.infra.storage;

import com.finance.core.model.User;
import com.finance.core.repository.UserRepository;

import java.io.*;
import java.util.*;

public class FileDataStorage implements UserRepository, DataStorage {
    private final Map<String, User> users;
    private final String dataFile;
    private boolean isModified;

    public FileDataStorage(String dataFile) {
        this.dataFile = dataFile;
        this.users = new HashMap<>();
        loadData();
        this.isModified = false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void loadData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))) {
            Map<String, User> loadedUsers = (Map<String, User>) ois.readObject();
            users.putAll(loadedUsers);
            System.out.println("Данные загружены. Пользователей: " + users.size());
        } catch (FileNotFoundException e) {
            System.out.println("Файл данных не найден, создается новый");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Ошибка при загрузке данных: " + e.getMessage());
        }
    }

    @Override
    public void saveData() {
        if (isModified) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFile))) {
                oos.writeObject(users);
                System.out.println("Данные сохранены. Пользователей: " + users.size());
                this.isModified = false;
            } catch (IOException e) {
                System.out.println("Ошибка при сохранении данных: " + e.getMessage());
            }
        }
    }

    @Override
    public User findByUsername(String username) {
        return users.get(username);
    }

    @Override
    public void save() {
        this.isModified = true;
    }

    @Override
    public void addUser(User user) {
        users.put(user.getUsername(), user);
        save();
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public boolean exists(String username) {
        return users.containsKey(username);
    }
}