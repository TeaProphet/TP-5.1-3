from rest_framework.exceptions import ValidationError


def complete_serialize(used_data: dict, serializer):
    serializing_object = serializer(data=used_data)
    if not serializing_object.is_valid():
        raise ValidationError
    return serializing_object.save()