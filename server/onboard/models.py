import firebase
from django.core.validators import MinLengthValidator
from django.db import models
from django.forms import ModelForm
from rest_framework import serializers
from rest_framework.exceptions import ValidationError


def minValidator(value, length=6):
	if len(str(value)) < length:
		raise ValidationError


# Create your models here.
class User(models.Model):
	login = models.CharField(max_length=255, validators=[MinLengthValidator(4)], default=None)
	password = models.CharField(max_length=255, validators=[MinLengthValidator(6)], default=None)
	idToken = models.CharField(max_length=1024, default=None)
	reputation = models.IntegerField(default=0)
	age = models.IntegerField(default=None)
	games = models.TextField(max_length=1024, default=None)
	vk = models.CharField(max_length=255, default=None)
	tg = models.CharField(max_length=255, default=None)
	played_sessions = models.JSONField(default=None)

	def register_firebase(self, database: firebase.Database, auth: firebase.Auth):
		if len(self.password) < 6:
			raise ValueError("WEAK_PASSWORD")
		users = database.child("users").get().each()
		if users:
			for user in users:
				if user.key() == self.login:
					raise ValueError("LOGIN_EXISTS")
		self.save_firebase(database, auth)

	def auth_firebase_credentials(self, database: firebase.Database):
		response = database.child("users").child(self.login).get()
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
		self.__init_user(database)

	def auth_firebase_token(self, database: firebase.Database, auth: firebase.Auth):
		find_response = database.child("users").order_by_child("idToken").equal_to(self.idToken).get().each()
		if len(find_response) != 0:
			self.login = find_response[0].key()
			self.__init_user(database)

	def __init_user(self, database: firebase.Database):
		user_path = database.child("users").child(self.login)
		self.reputation = user_path.child("reputation").get().val()
		self.age = user_path.child("age").get().val()
		self.games = user_path.child("games").get().val()
		self.vk = user_path.child("vk").get().val()
		self.tg = user_path.child("tg").get().val()
		self.played_sessions = user_path.child("played_sessions").get().val()

	def save_firebase(self, database: firebase.Database, auth: firebase.Auth):
		self.idToken = auth.create_custom_token(database.generate_key())
		database.child("users").set(
			{self.login: {"password": self.password, "idToken": self.idToken, "reputation": self.reputation,
			              "age": self.age, "games": self.games, "vk": self.vk, "tg": self.tg,
			              "played_sessions": self.played_sessions}})


class UserSerializer(serializers.Serializer):
	login = serializers.CharField(max_length=255, default=None)
	password = serializers.CharField(max_length=255, default=None)
	idToken = serializers.CharField(max_length=1024, default=None)
	reputation = serializers.IntegerField(default=0)
	age = serializers.IntegerField(default=None)
	games = serializers.CharField(max_length=1024, default=None)
	vk = serializers.CharField(max_length=255, default=None)
	tg = serializers.CharField(max_length=255, default=None)
	played_sessions = serializers.JSONField(default=None)

	def create(self, validated_data):
		return User(**validated_data)

	def update(self, instance, validated_data):
		instance.authorize = validated_data.get('login', instance.authorize)
		instance.password = validated_data.get('password', instance.password)
		instance.idToken = validated_data.get('idToken', instance.idToken)
		return instance


class UserForm(ModelForm):
	class Meta:
		model = User
		exclude = ['login', 'password', 'idToken']
