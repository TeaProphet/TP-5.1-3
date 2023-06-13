from drf_spectacular.utils import OpenApiExample

invalid_data = OpenApiExample(
	'Некорректные данные',
	summary="Ошибка: некорректные данные",
	description="Ошибка, возникающая при неверно введённых значениях. Возвращает код ошибки error.",
	value={
		'error': 'INVALID_DATA'
	},
	response_only=True,
	status_codes=[400]
)
