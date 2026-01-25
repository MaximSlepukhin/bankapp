{{- define "notifications-service.name" -}}
notifications-service
{{- end }}

{{- define "notifications-service.fullname" -}}
notifications-service
{{- end }}

{{- define "notifications-service.selectorLabels" -}}
app: notifications-service
{{- end }}

{{- define "notifications-service.podLabels" -}}
app: notifications-service
release: notifications-service
{{- end }}

{{- define "notifications-service.configName" -}}
notifications-service-config
{{- end }}
