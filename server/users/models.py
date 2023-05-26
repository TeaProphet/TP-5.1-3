from django.core.validators import MinLengthValidator
from django.db import models
from rest_framework import serializers

from onboardProject import settings


class UserData(models.Model):
    reputation = models.IntegerField(default=0)
    age = models.IntegerField(default=None)
    games = models.TextField(max_length=1024, default=None)
    vk = models.CharField(max_length=255, default=None)
    tg = models.CharField(max_length=255, default=None)
    played_sessions = models.JSONField(default=None)
    password = models.CharField(max_length=255, default=None)


class UserDataSerializer(serializers.Serializer):
    reputation = serializers.IntegerField(default=0)
    age = serializers.IntegerField(default=None)
    games = serializers.CharField(max_length=1024, default=None)
    vk = serializers.CharField(max_length=255, default=None)
    tg = serializers.CharField(max_length=255, default=None)
    played_sessions = serializers.JSONField(default=None)
    password = serializers.CharField(max_length=255, default=None)

    def create(self, validated_data):
        return UserData(**validated_data)



class UserCredentials(models.Model):
    login = models.CharField(max_length=255, validators=[MinLengthValidator(3)], default=None)
    password = models.CharField(max_length=255, validators=[MinLengthValidator(6)], default=None)
    idToken = models.CharField(max_length=255, default=None)
    user_data = None

    def register_firebase(self):
        users = settings.database.child("users").get().each()
        if users:
            for user in users:
                if user.key() == self.login:
                    raise ValueError("LOGIN_EXISTS")
        self.save_firebase()

    def auth_firebase_credentials(self):
        response = settings.database.child("users").child(self.login).get()
        if response.each() is not None:
            for field in response.each():
                if field.key() == "password":
                    if self.password != field.val():
                        raise ValueError
                    break
            for field in response.each():
                if field.key() == "idToken":
                    self.idToken = field.val()
                    break
        else:
            raise ValueError

    def auth_firebase_token(self):
        find_response = settings.database.child("users").order_by_child("idToken").equal_to(self.idToken).get().each()
        if len(find_response) != 0:
            self.login = find_response[0].key()
            self.password = find_response[0].val()['password']
        else:
            raise ValueError

    def fetch_user_data(self):
        user_data = UserData()
        user_data.reputation = settings.database.child("users").child(self.login).child("reputation").get().val()
        user_data.age = settings.database.child("users").child(self.login).child("age").get().val()
        user_data.games = settings.database.child("users").child(self.login).child("games").get().val()
        user_data.vk = settings.database.child("users").child(self.login).child("vk").get().val()
        user_data.tg = settings.database.child("users").child(self.login).child("tg").get().val()
        user_data.played_sessions = settings.database.child("users").child(self.login).child("played_sessions").get().val()
        self.user_data = user_data

    def update_user_data(self):
        data = UserDataSerializer(self.user_data).data
        if not data['password']:
            data.pop('password')
        settings.database.child("users").child(self.login).update(data)

    def save_firebase(self):
        self.idToken = settings.auth.create_custom_token(settings.database.generate_key())
        self.user_data = UserData()
        self.user_data.password = self.password
        settings.database.child("users").update(
            {self.login: UserDataSerializer(self.user_data).data})
        settings.database.child("users").child(self.login).update({'idToken': self.idToken})


class UserCredentialsSerializer(serializers.Serializer):
    login = serializers.CharField(max_length=255, validators=[MinLengthValidator(3)], default=None)
    password = serializers.CharField(max_length=255, validators=[MinLengthValidator(6)], default=None)
    idToken = serializers.CharField(max_length=1024, default=None)

    def create(self, validated_data):
        return UserCredentials(**validated_data)
