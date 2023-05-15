import json
import firebase
from django.core import serializers
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from rest_framework.exceptions import ValidationError

from onboard import models

# Create your views here.
config = json.load(open('onboard/onboard_config.json'))
app = firebase.initialize_app(config)
auth = app.auth()
database = app.database()


@csrf_exempt
def register(request):
    try:
        body_unicode = request.body.decode('utf-8')
        body_data = json.loads(body_unicode)
        print(body_data)
        user = models.UserSerializer(data=body_data)
        if not user.is_valid():
            raise ValidationError
        user = user.save()
    except ValidationError:
        return JsonResponse({'error': 'INVALID_CREDENTIALS'})
    try:
        user.register_firebase(database, auth)
    except ValueError as exception:
        return JsonResponse({'error': str(exception)})
    return JsonResponse({'idToken': user.idToken})

@csrf_exempt
def credentials_authorize(request):
    try:
        body_unicode = request.body.decode('utf-8')
        body_data = json.loads(body_unicode)
        user = models.UserSerializer(data=body_data)
        if not user.is_valid():
            raise ValidationError
        user = user.save()
    except ValidationError:
        return JsonResponse({'error': 'INVALID_CREDENTIALS'})
    try:
        user.auth_firebase_credentials(database)
    except ValueError:
        return JsonResponse({'error': 'WRONG_CREDENTIALS'})
    return JsonResponse({'idToken': user.idToken})

@csrf_exempt
def token_authorize(request):
    try:
        body_unicode = request.body.decode('utf-8')
        body_data = json.loads(body_unicode)
        user = models.UserSerializer(data=body_data)
        if not user.is_valid():
            raise ValidationError
        user = user.save()
    except ValidationError:
        return JsonResponse({'error': 'INVALID_CREDENTIALS'})
    try:
        user.auth_firebase_token(database, auth)
    except ValueError:
        return JsonResponse({'error': 'WRONG_CREDENTIALS'})
    data = serializers.serialize('json', [user, ])
    struct = json.loads(data)
    del struct[0]["fields"]["password"]
    return JsonResponse(struct[0]["fields"], safe=False)

