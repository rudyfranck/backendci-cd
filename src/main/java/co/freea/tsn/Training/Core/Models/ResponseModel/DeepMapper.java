package co.freea.tsn.Training.Core.Models.ResponseModel;

import co.freea.tsn.Training.Core.Models.CommonService;
import co.freea.tsn.Training.Core.Models.DeepType;
import co.freea.tsn.Training.Core.Service.IService;
import co.freea.tsn.Training.Core.Util.Logger;
import lombok.RequiredArgsConstructor;
import org.davidmoten.rx.jdbc.Database;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class DeepMapper {

    private final Database database;

    public <T extends CommonService> Optional<JSONObject> toMap(T intrance) {
        Logger.debug("DeepMapper|Map");
        JSONObject object = new JSONObject();

        for (Field field : intrance.getClass().getDeclaredFields()) {
            if (field == null) {
                continue;
            }

            boolean state = field.isAccessible();
            field.setAccessible(true);
            if (field.getType().getName().contentEquals(IService.class.getName())) {
                continue;
            }
            try {
                if (!field.getName().contentEquals("id") && field.getType().getName().contentEquals(UUID.class.getName())) {
                    DeepType link = field.getAnnotation(DeepType.class);
                    if (link != null && Optional.ofNullable(field.get(intrance)).isPresent()) {
                        if (Optional.ofNullable(this.database).isPresent()) {
                            try {

                                String sql = "SELECT * FROM " + link.table() + " WHERE id = '" + field.get(intrance) + "'";

                                Logger.debug(sql);

                                Optional<? extends CommonService> common = database
                                        .select(sql)
                                        .get(rs -> analyseForObject(rs, link.deepLink(), false)).blockingFirst();

                                if (common != null && common.isPresent()) {
                                    object.put(field.getName(), toMap(common.get()).orElse(null));
                                } else {
                                    object.put(field.getName(), Optional.empty());
                                }
                                continue;
                            } catch (Exception e) {
                                Logger.debug(e.getMessage());
                            }
                        }
                    }
                }
                object.put(field.getName(), field.get(intrance));
            } catch (Exception e) {
                Logger.debug("DeepMapper+0", e.getMessage());
            } finally {
                field.setAccessible(state);
            }
        }
        return Optional.of(object);
    }

    public static <T extends CommonService> Optional<T> analyseForObject(ResultSet set, Class<?> _class, boolean strict) {
        Logger.debug("DeepMapper|analyseForObject");
        if (Optional.ofNullable(set).isPresent() && Optional.ofNullable(_class).isPresent()) {

            Field[] fields = _class.getDeclaredFields();

            try {

                T instance = (T) _class.getDeclaredConstructor().newInstance();

                if (strict) {
                    boolean next = set.next();
                    if (!next) {
                        return Optional.empty();
                    }
                }

                for (Field field : fields) {
                    boolean state = field.isAccessible();
                    field.setAccessible(true);
                    try {
                        Type type = field.getGenericType();
                        if (type.equals(Long.class)) {
                            field.setLong(instance, set.getLong(field.getName()));
                        } else if (type.equals(Integer.class)) {
                            field.setInt(instance, set.getInt(field.getName()));
                        } else if (type.equals(Boolean.class)) {
                            field.setBoolean(instance, set.getBoolean(field.getName()));
                        } else if (type.equals(UUID.class)) {
                            field.set(instance, UUID.fromString(set.getString(field.getName())));
                        } else if (type.equals(String.class)) {
                            field.set(instance, set.getString(field.getName()));
                        } else if (type.equals(Timestamp.class)) {
                            field.set(instance, set.getTimestamp(field.getName()));
                        }
                    } catch (Exception e) {
//                        Logger.debug("Inner", e.getMessage());
                    } finally {
                        field.setAccessible(state);
                    }
                }

                Logger.debug(instance);
                return Optional.of(instance);
            } catch (Exception e) {
                Logger.debug("Outer", e.getMessage());
            }
        }
        Logger.debug("Done without nothing");
        return Optional.empty();
    }
}
