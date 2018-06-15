#!/usr/bin/env python3
import requests
from html.parser import HTMLParser
import json
import sys


class LoginTokenParser(HTMLParser):
    def handle_starttag(self, tag, attrs):
        if tag == 'input':
            attr_dict = dict(attrs)
            if 'name' in attr_dict:
                if attr_dict['name'] == '_token':
                    token = attr_dict['value']
                    self.data = {'token': token}


class AutarcoCurrentProductionRetriever:
    def __init__(self, username, password, session_cookie_filename):
        self.username = username
        self.password = password
        self.filename = session_cookie_filename
        self.login_url = 'https://my.autarco.com/auth/login'
        self.site_url = 'https://my.autarco.com/api/site'
        self.data_today_url_format = 'https://my.autarco.com/api/site/{}'

    def get_data_today(self):
        self.__load_user_data()
        self.__get_login_token_and_cookies()
        self.__login()
        self.__get_available_sites()
        data = self.__get_data_today()
        self.__persist_user_data()
        return data

    def __load_user_data(self):
        print('Loading user data')
        try:
            with open(self.filename, 'r') as cookie_storage:
                data = json.load(cookie_storage)
                if 'session_cookie' in data:
                    self.session_cookie = data['session_cookie']
                    print('Loaded session cookie from storage')
                if 'available_sites' in data:
                    self.available_sites = data['available_sites']
                    print('Loaded available sites from storage')
        except (FileNotFoundError, json.JSONDecodeError):
            pass

    def __persist_user_data(self):
        print('Persisting user data')
        with open(self.filename, 'w') as storage_file:
            data = dict()
            if 'session_cookie' in self.__dict__:
                data['session_cookie'] = self.session_cookie
            if 'available_sites' in self.__dict__:
                data['available_sites'] = self.available_sites
            json.dump(data, storage_file)

    def __get_login_token_and_cookies(self):
        if 'session_cookie' in self.__dict__:
            return
        print('Getting login token and session + xsrf cookies')
        r = requests.get(self.login_url)
        parser = LoginTokenParser()
        parser.feed(r.text)
        self.login_token = parser.data['token']
        self.cookies = r.cookies

    def __login(self):
        if 'session_cookie' in self.__dict__:
            return
        print('Logging in')
        params = {'_token': self.login_token,
                  'username': self.username, 'password': self.password}
        r = requests.post(self.login_url,
                          params=params, cookies=self.cookies)
        self.session_cookie = r.cookies['autarco_session']

    def __get_available_sites(self):
        if 'available_sites' in self.__dict__:
            return
        print('Retrieving available sites')
        if 'session_cookie' in self.__dict__:
            r = requests.get(self.site_url, cookies={
                             "autarco_session": self.session_cookie})
            if r.status_code == 401:
                print('Invalid token')
                del self.session_cookie
                return
            sites_json = r.json()
            sites = list()
            for site in sites_json:
                sites.append(site['public_key'])
            self.available_sites = sites

    def __get_data_today(self):
        print('Getting todays data')
        data_from_all_sites = list()
        if 'session_cookie' in self.__dict__ and 'available_sites' in self.__dict__:
            for available_site in self.available_sites:
                r = requests.get(
                    self.data_today_url_format.format(available_site), cookies={"autarco_session": self.session_cookie})
                if r.status_code == 401:
                    print('Invalid token')
                    del self.session_cookie
                    return list()
                data_from_all_sites.append(r.text)
        else:
            print('No available sites or session cookie not retrieved succesfully')
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
