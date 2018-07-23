#!/usr/bin/env python3
import requests
import json
import sys


class AutarcoCurrentProductionRetriever:
    def __init__(self, username, password, user_data_filename):
        self.username = username
        self.password = password
        self.filename = user_data_filename
        self.site_url = 'https://my.autarco.com/api/site'
        self.data_today_url_format = 'https://my.autarco.com/api/site/{}'

    def get_data_today(self):
        self.__load_user_data()
        self.__get_available_sites()
        data = self.__get_data_today()
        self.__persist_user_data()
        return data

    def __load_user_data(self):
        print('Loading user data')
        try:
            with open(self.filename, 'r') as user_data_storage:
                data = json.load(user_data_storage)
                if 'available_sites' in data:
                    self.available_sites = data['available_sites']
                    print('Loaded available sites from storage')
        except (FileNotFoundError, json.JSONDecodeError):
            pass

    def __persist_user_data(self):
        print('Persisting user data')
        with open(self.filename, 'w') as storage_file:
            data = dict()
            if 'available_sites' in self.__dict__:
                data['available_sites'] = self.available_sites
            json.dump(data, storage_file)

    def __get_available_sites(self):
        if 'available_sites' in self.__dict__:
            return
        print('Retrieving available sites')
        r = requests.get(self.site_url, auth=(self.username, self.password))
        if r.status_code == 401:
            print(r.text)
            print('Invalid username or password')
            return
        sites_json = r.json()
        sites = list()
        for site in sites_json:
            sites.append(site['public_key'])
        self.available_sites = sites

    def __get_data_today(self):
        print('Getting todays data')
        data_from_all_sites = list()
        if 'available_sites' in self.__dict__:
            for available_site in self.available_sites:
                r = requests.get(
                    self.data_today_url_format.format(available_site), auth=(self.username, self.password))
                if r.status_code == 401:
                    print('Invalid username or password')
                    return list()
                data_from_all_sites.append(r.text)
        else:
            print('No available sites')
            return list()
        return data_from_all_sites


def save_data(data):
    print('Saving data to file')
    with open('data.json', 'a') as data_file:
        data_file.writelines(data)
        data_file.write("\n")


def main():
    try:
        with open('login_data.json', 'r') as login_data_file:
            login_data = json.load(login_data_file)
            user_name = login_data['user_name']
            password = login_data['password']
    except (FileNotFoundError, json.JSONDecodeError):
        print("Error while opening login data file, maybe it doesn't exist\n"
              "An example should be available with the name login_data.json.example\n"
              "Adapt that file to your needs")
        sys.exit(1)
    data_retriever = AutarcoCurrentProductionRetriever(
        user_name, password, 'user_data.json')
    data = data_retriever.get_data_today()
    # print(data)
    save_data(data)


if __name__ == "__main__":
    main()
